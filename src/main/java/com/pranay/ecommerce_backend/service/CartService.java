package com.pranay.ecommerce_backend.service;

import com.pranay.ecommerce_backend.dto.cart.AddCartItemRequest;
import com.pranay.ecommerce_backend.dto.cart.CartResponse;
import com.pranay.ecommerce_backend.dto.cart.UpdateCartItemRequest;

public interface CartService {

    CartResponse addItem(String userEmail, AddCartItemRequest request);

    CartResponse updateItemQuantity(String userEmail, Long cartItemId, UpdateCartItemRequest request);

    CartResponse removeItem(String userEmail, Long cartItemId);

    CartResponse getCart(String userEmail);
}
