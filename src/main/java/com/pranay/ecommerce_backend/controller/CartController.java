package com.pranay.ecommerce_backend.controller;

import com.pranay.ecommerce_backend.dto.cart.AddCartItemRequest;
import com.pranay.ecommerce_backend.dto.cart.CartResponse;
import com.pranay.ecommerce_backend.dto.cart.UpdateCartItemRequest;
import com.pranay.ecommerce_backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@AuthenticationPrincipal UserDetails userDetails,
                                                @Valid @RequestBody AddCartItemRequest request) {
        log.info("Add item request by user: {}", userDetails.getUsername());
        CartResponse response = cartService.addItem(userDetails.getUsername(), request);
        log.info("Item added to cart successfully. cartId: {}", response.getCartId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(@AuthenticationPrincipal UserDetails userDetails,
                                                           @PathVariable Long cartItemId,
                                                           @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("Update cart item request by user: {}, cartItemId: {}", userDetails.getUsername(), cartItemId);
        CartResponse response = cartService.updateItemQuantity(userDetails.getUsername(), cartItemId, request);
        log.info("Cart item updated successfully. cartItemId: {}", cartItemId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItem(@AuthenticationPrincipal UserDetails userDetails,
                                                   @PathVariable Long cartItemId) {
        log.info("Remove cart item request by user: {}, cartItemId: {}", userDetails.getUsername(), cartItemId);
        CartResponse response = cartService.removeItem(userDetails.getUsername(), cartItemId);
        log.info("Cart item removed successfully. cartItemId: {}", cartItemId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get cart request by user: {}", userDetails.getUsername());
        CartResponse response = cartService.getCart(userDetails.getUsername());
        log.info("Cart fetched successfully. cartId: {}", response.getCartId());
        return ResponseEntity.ok(response);
    }
}