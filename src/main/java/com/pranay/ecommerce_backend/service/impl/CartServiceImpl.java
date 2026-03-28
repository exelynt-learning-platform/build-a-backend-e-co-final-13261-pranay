package com.pranay.ecommerce_backend.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.pranay.ecommerce_backend.dto.cart.AddCartItemRequest;
import com.pranay.ecommerce_backend.dto.cart.CartItemResponse;
import com.pranay.ecommerce_backend.dto.cart.CartResponse;
import com.pranay.ecommerce_backend.dto.cart.UpdateCartItemRequest;
import com.pranay.ecommerce_backend.entity.*;
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
        log.debug("Adding item to cart. userEmail={}, productId={}, quantity={}", userEmail, request.getProductId(), request.getQuantity());
        User user = getUser(userEmail);
        Cart cart = getOrCreateCart(user);
        Product product = getProduct(request.getProductId());

        CartItem cartItem = getOrCreateCartItem(cart, product);
        int newQuantity = cartItem.getQuantity() + request.getQuantity();
        validateStock(product, newQuantity);

        cartItem.setQuantity(newQuantity);
        if (cartItem.getId() == null) {
            cart.getItems().add(cartItem);
            log.debug("New cart item added to cart. productId={}", product.getId());
        }

        cartItemRepository.save(cartItem);
        log.debug("Cart item saved. cartItemId={}, newQuantity={}", cartItem.getId(), cartItem.getQuantity());

        Cart detailedCart = getDetailedCart(user);
        log.debug("Returning updated cart for userEmail={}", userEmail);
        return mapCartResponse(detailedCart);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(String userEmail, Long cartItemId, UpdateCartItemRequest request) {
        log.debug("Updating cart item quantity. userEmail={}, cartItemId={}, newQuantity={}", userEmail, cartItemId, request.getQuantity());
        CartItem cartItem = getOwnedCartItem(userEmail, cartItemId);
        validateStock(cartItem.getProduct(), request.getQuantity());
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        log.debug("Cart item quantity updated. cartItemId={}, updatedQuantity={}", cartItemId, request.getQuantity());

        return mapCartResponse(getDetailedCart(cartItem.getCart().getUser()));
    }

    @Override
    @Transactional
    public CartResponse removeItem(String userEmail, Long cartItemId) {
        log.debug("Removing cart item. userEmail={}, cartItemId={}", userEmail, cartItemId);
        CartItem cartItem = getOwnedCartItem(userEmail, cartItemId);
        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        log.debug("Cart item removed. cartItemId={}", cartItemId);

        return mapCartResponse(getDetailedCart(cart.getUser()));
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String userEmail) {
        log.debug("Fetching cart for userEmail={}", userEmail);
        User user = getUser(userEmail);
        return mapCartResponse(getDetailedCart(user));
    }

    // --- private helpers ---

    private CartItem getOrCreateCartItem(Cart cart, Product product) {
        return cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(CartItem.builder().cart(cart).product(product).quantity(0).build());
    }

    private CartItem getOwnedCartItem(String userEmail, Long cartItemId) {
        User user = getUser(userEmail);
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));
        if (item.getCart() == null || !item.getCart().getId().equals(cart.getId())) {
            throw new ValidationException("You can only modify your own cart");
        }
        return item;
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = cartRepository.save(Cart.builder().user(user).build());
                    log.debug("Created new cart for userId={}", user.getId());
                    return newCart;
                });
    }

    private Cart getDetailedCart(User user) {
        return cartRepository.findDetailedByUserId(user.getId())
                .orElseGet(() -> getOrCreateCart(user));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private void validateStock(Product product, int quantity) {
        if (quantity > product.getStockQuantity()) {
            throw new ValidationException("Insufficient stock for product: " + product.getName());
        }
    }

    private CartResponse mapCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(item -> CartItemResponse.builder()
                .cartItemId(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .unitPrice(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .lineTotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .build()).toList();

        BigDecimal totalAmount = items.stream().map(CartItemResponse::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }
}