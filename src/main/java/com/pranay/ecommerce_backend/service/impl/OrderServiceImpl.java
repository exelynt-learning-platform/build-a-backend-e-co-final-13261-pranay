package com.pranay.ecommerce_backend.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.pranay.ecommerce_backend.dto.order.CreateOrderRequest;
import com.pranay.ecommerce_backend.dto.order.OrderItemResponse;
import com.pranay.ecommerce_backend.dto.order.OrderResponse;
import com.pranay.ecommerce_backend.entity.*;
import com.pranay.ecommerce_backend.exception.ResourceNotFoundException;
import com.pranay.ecommerce_backend.exception.ValidationException;
import com.pranay.ecommerce_backend.repository.CartRepository;
import com.pranay.ecommerce_backend.repository.OrderRepository;
import com.pranay.ecommerce_backend.repository.ProductRepository;
import com.pranay.ecommerce_backend.repository.UserRepository;
import com.pranay.ecommerce_backend.service.InventoryService;
import com.pranay.ecommerce_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrderResponse createOrder(String userEmail, CreateOrderRequest request) {
        log.info("Creating order for user: {}", userEmail);

        log.debug("Shipping address: {}", request.getShippingAddress());

        User user = getUser(userEmail);

        Cart cart = cartRepository.findDetailedByUserId(user.getId())
                .orElseThrow(() -> {
                    log.error("Cart not found for userId: {}", user.getId());
                    return new ResourceNotFoundException("Cart not found for user");
                });

        if (cart.getItems().isEmpty()) {
            log.warn("Cart is empty for userId: {}", user.getId());
            throw new ValidationException("Cart is empty");
        }

        // Step 1: Initialize order
        CustomerOrder order = initializeOrder(user, request.getShippingAddress());

        // Step 2: Reserve stock (deduct temporarily)
        BigDecimal totalPrice = BigDecimal.ZERO;

        //  SINGLE LOOP (critical fix)
        for (CartItem cartItem : cart.getItems()) {

            Product product = loadProductForUpdate(cartItem.getProduct().getId());

            inventoryService.reserveStock(product, cartItem.getQuantity());

            totalPrice = totalPrice.add(
                    product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );

            order.getItems().add(
                    OrderItem.builder()
                            .order(order)
                            .product(product)
                            .quantity(cartItem.getQuantity())
                            .price(product.getPrice())
                            .build()
            );
        }

        order.setTotalPrice(totalPrice);

        CustomerOrder savedOrder = orderRepository.save(order);
        orderRepository.flush();

        // clear cart
        cart.getItems().clear();

        log.info("Order created successfully with id: {}", savedOrder.getId());

        return mapOrderResponse(savedOrder);
    }

    private CustomerOrder initializeOrder(User user, String shippingAddress) {
        return CustomerOrder.builder()
                .user(user)
                .shippingAddress(shippingAddress)
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
    }

    private Product loadProductForUpdate(Long productId) {
        return productRepository.findWithLockById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForUser(String userEmail) {
        User user = getUser(userEmail);
        log.info("Fetching orders for user: {}", userEmail);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::mapOrderResponse)
                .toList();

    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String userEmail, Long orderId) {
        User user = getUser(userEmail);
        log.info("Fetching orderId: {} for user: {}", orderId, userEmail);
        CustomerOrder order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapOrderResponse(order);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private OrderResponse mapOrderResponse(CustomerOrder order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .paymentIntentId(order.getPaymentIntentId())
                .items(order.getItems().stream()
                        .map(item -> OrderItemResponse.builder()
                                .productId(item.getProduct().getId())
                                .productName(item.getProduct().getName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build())
                        .toList())
                .build();
    }
}