package com.pranay.ecommerce_backend.controller;

import com.pranay.ecommerce_backend.dto.order.CreateOrderRequest;
import com.pranay.ecommerce_backend.dto.order.OrderResponse;
import com.pranay.ecommerce_backend.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j // Lombok logger for info
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal UserDetails userDetails,
                                                     @Valid @RequestBody CreateOrderRequest request) {
        log.info("Create order request by user: {}", userDetails.getUsername());
        OrderResponse response = orderService.createOrder(userDetails.getUsername(), request);
        log.info("Order created successfully. orderId: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Get all orders request by user: {}", userDetails.getUsername());
        List<OrderResponse> response = orderService.getOrdersForUser(userDetails.getUsername());
        log.info("Fetched {} orders for user: {}", response.size(), userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@AuthenticationPrincipal UserDetails userDetails,
                                                      @PathVariable Long orderId) {
        log.info("Get order by id request by user: {}, orderId: {}", userDetails.getUsername(), orderId);
        OrderResponse response = orderService.getOrderById(userDetails.getUsername(), orderId);
        log.info("Fetched order successfully. orderId: {}", response.getId());
        return ResponseEntity.ok(response);
    }
}