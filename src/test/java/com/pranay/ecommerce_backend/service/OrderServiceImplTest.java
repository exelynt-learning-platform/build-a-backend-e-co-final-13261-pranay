package com.pranay.ecommerce_backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import com.pranay.ecommerce_backend.dto.order.CreateOrderRequest;
import com.pranay.ecommerce_backend.dto.order.OrderResponse;
import com.pranay.ecommerce_backend.entity.*;
import com.pranay.ecommerce_backend.exception.ValidationException;
import com.pranay.ecommerce_backend.repository.CartRepository;
import com.pranay.ecommerce_backend.repository.OrderRepository;
import com.pranay.ecommerce_backend.repository.ProductRepository;
import com.pranay.ecommerce_backend.repository.UserRepository;
import com.pranay.ecommerce_backend.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrderShouldConvertCartIntoOrderAndClearCart() {
        User user = User.builder().id(1L).email("user@example.com").role(Role.USER).build();
        Product product = Product.builder()
                .id(2L)
                .name("Keyboard")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(4)
                .build();
        CartItem cartItem = CartItem.builder().id(3L).product(product).quantity(2).build();
        Cart cart = Cart.builder().id(4L).user(user).items(new ArrayList<>()).build();
        cartItem.setCart(cart);
        cart.getItems().add(cartItem);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Spring Street");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findWithLockById(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(CustomerOrder.class)))
                .thenAnswer(invocation -> {
                    CustomerOrder order = invocation.getArgument(0);
                    order.setId(99L);
                    return order;
                });

        OrderResponse response = orderService.createOrder(user.getEmail(), request);

        assertEquals(99L, response.getId());
        assertEquals(BigDecimal.valueOf(200), response.getTotalPrice());
        assertEquals(2, product.getStockQuantity());
        assertEquals(0, cart.getItems().size(), "Cart should be cleared after order creation");
        verify(productRepository, times(1)).findWithLockById(product.getId());
    }

    @Test
    void createOrderShouldFailForEmptyCart() {
        User user = User.builder().id(1L).email("user@example.com").role(Role.USER).build();
        Cart cart = Cart.builder().id(4L).user(user).items(new ArrayList<>()).build();
        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Spring Street");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        assertThrows(ValidationException.class, () -> orderService.createOrder(user.getEmail(), request));
    }
}
