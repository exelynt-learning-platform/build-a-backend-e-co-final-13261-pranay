package com.pranay.ecommerce_backend.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.pranay.ecommerce_backend.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponse {

    private final Long id;
    private final Long userId;
    private final BigDecimal totalPrice;
    private final OrderStatus status;
    private final String shippingAddress;
    private final LocalDateTime createdAt;
    private final String paymentIntentId;
    private final List<OrderItemResponse> items;
}
