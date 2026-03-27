package com.pranay.ecommerce_backend.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.pranay.ecommerce_backend.dto.cart.AddCartItemRequest;
import com.pranay.ecommerce_backend.dto.cart.CartItemResponse;
import com.pranay.ecommerce_backend.dto.cart.CartResponse;
import com.pranay.ecommerce_backend.dto.cart.UpdateCartItemRequest;
import com.pranay.ecommerce_backend.entity.Cart;
import com.pranay.ecommerce_backend.entity.CartItem;
import com.pranay.ecommerce_backend.entity.Product;
import com.pranay.ecommerce_backend.entity.User;
import com.pranay.ecommerce_backend.exception.ResourceNotFoundException;
import com.pranay.ecommerce_backend.exception.ValidationException;
import com.pranay.ecommerce_backend.repository.CartItemRepository;
import com.pranay.ecommerce_backend.repository.CartRepository;
import com.pranay.ecommerce_backend.repository.ProductRepository;
import com.pranay.ecommerce_backend.repository.UserRepository;
import com.pranay.ecommerce_backend.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CartResponse addItem(String userEmail, AddCartItemRequest request) {
        log.debug("Add item request by user: {} for productId: {}, quantity: {}", userEmail, request.getProductId(), request.getQuantity());

        User user = getUser(userEmail);
        Cart cart = getOrCreateCart(user);
        Product product = getProduct(request.getProductId());

        CartItem cartItem = getOrCreateCartItem(cart, product);

        int newQuantity = cartItem.getQuantity() + request.getQuantity();
        validateStock(product, newQuantity);

        cartItem.setQuantity(newQuantity);

        if (cartItem.getId() == null) {
            cart.getItems().add(cartItem);
        }

        cartItemRepository.save(cartItem);
        log.debug("Cart item saved. cartItemId: {}, newQuantity: {}", cartItem.getId(), cartItem.getQuantity());

        return mapCartResponse(getDetailedCart(user, cart));
    }

    private CartItem getOrCreateCartItem(Cart cart, Product product) {
        return cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(CartItem.builder()
                        .cart(cart)
                        .product(product)
                        .quantity(0)
                        .build());
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(String userEmail, Long cartItemId, UpdateCartItemRequest request) {
        log.debug("Update cart item request by user: {} for cartItemId: {}, newQuantity: {}", userEmail, cartItemId, request.getQuantity());

        CartItem cartItem = getOwnedCartItem(userEmail, cartItemId);
        validateStock(cartItem.getProduct(), request.getQuantity());
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        log.debug("Cart item updated. cartItemId: {}, quantity: {}", cartItem.getId(), cartItem.getQuantity());
        return mapCartResponse(cartItem.getCart());
    }

    @Override
    @Transactional
    public CartResponse removeItem(String userEmail, Long cartItemId) {
        log.debug("Remove cart item request by user: {} for cartItemId: {}", userEmail, cartItemId);

        CartItem cartItem = getOwnedCartItem(userEmail, cartItemId);
        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));

        log.debug("Cart item removed. cartItemId: {}", cartItemId);
        return mapCartResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String userEmail) {
        log.debug("Get cart request by user: {}", userEmail);

        User user = getUser(userEmail);
        Cart cart = getOrCreateCart(user);

        return mapCartResponse(getDetailedCart(user, cart));
    }

    private CartItem getOwnedCartItem(String userEmail, Long cartItemId) {
        User user = getUser(userEmail);
        Cart userCart = getOrCreateCart(user);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));
        if (cartItem.getCart() == null || !cartItem.getCart().getId().equals(userCart.getId())) {
            throw new ValidationException("You can only modify your own cart");
        }
        return cartItem;
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private Cart getDetailedCart(User user, Cart fallbackCart) {
        Optional<Cart> detailedCart = cartRepository.findDetailedByUserId(user.getId());
        return detailedCart.orElse(fallbackCart);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private void validateStock(Product product, Integer quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new ValidationException("Insufficient stock for product: " + product.getName());
        }
    }

    private CartResponse mapCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .cartItemId(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .unitPrice(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .lineTotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }
}