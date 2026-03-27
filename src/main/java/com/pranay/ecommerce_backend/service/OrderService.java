package com.pranay.ecommerce_backend.service;

import com.pranay.ecommerce_backend.dto.order.CreateOrderRequest;
import com.pranay.ecommerce_backend.dto.order.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(String userEmail, CreateOrderRequest request);

    List<OrderResponse> getOrdersForUser(String userEmail);

    OrderResponse getOrderById(String userEmail, Long orderId);
}
