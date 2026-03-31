package com.pranay.ecommerce_backend.service.impl;

import java.math.BigDecimal;
import java.util.List;

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

        User user = getUserOrThrow(userEmail);
        Cart cart = getOrCreateCart(user);
        Product product = getProductOrThrow(request.getProductId());

        CartItem cartItem = getOrCreateCartItem(cart, product);

        int newQuantity = cartItem.getQuantity() + request.getQuantity();
        validateStock(product, newQuantity);

        cartItem.setQuantity(newQuantity);

        if (cartItem.getId() == null) {
            cart.getItems().add(cartItem);
        }

        cartItemRepository.save(cartItem);

        return mapCartResponse(getDetailedCartOrThrow(user));
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(String userEmail, Long cartItemId, UpdateCartItemRequest request) {
        log.debug("Updating cart item quantity. userEmail={}, cartItemId={}, newQuantity={}", userEmail, cartItemId, request.getQuantity());

        CartItem cartItem = getOwnedCartItem(userEmail, cartItemId);

        validateStock(cartItem.getProduct(), request.getQuantity());

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        return mapCartResponse(getDetailedCartOrThrow(cartItem.getCart().getUser()));
    }

    @Override
    @Transactional
    public CartResponse removeItem(String userEmail, Long cartItemId) {
        log.debug("Removing cart item. userEmail={}, cartItemId={}", userEmail, cartItemId);

        CartItem cartItem = getOwnedCartItem(userEmail, cartItemId);

        Cart cart = cartItem.getCart();

        cartItemRepository.delete(cartItem);
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));

        return mapCartResponse(getDetailedCartOrThrow(cart.getUser()));
    }

    @Override
    @Transactional
    public CartResponse getCart(String userEmail) {
        log.debug("Fetching cart for userEmail={}", userEmail);

        User user = getUserOrThrow(userEmail);

        Cart cart = cartRepository.findDetailedByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        return mapCartResponse(cart);
    }

    // ================= CLEAN HELPERS =================

    private User getUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Product getProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private Cart getCartOrThrow(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private Cart getDetailedCartOrThrow(User user) {
        return cartRepository.findDetailedByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + user.getId()));
    }

    private CartItem getOwnedCartItem(String userEmail, Long cartItemId) {
        User user = getUserOrThrow(userEmail);
        Cart cart = getCartOrThrow(user.getId());

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + cartItemId));

        validateCartOwnership(cart, item);

        return item;
    }

    private void validateCartOwnership(Cart cart, CartItem item) {
        if (item.getCart() == null || !item.getCart().getId().equals(cart.getId())) {
            throw new ValidationException("You can only modify your own cart");
        }
    }

    private CartItem getOrCreateCartItem(Cart cart, Product product) {
        return cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(CartItem.builder().cart(cart).product(product).quantity(0).build());
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