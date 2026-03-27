package com.pranay.ecommerce_backend.dto.payment;

import com.pranay.ecommerce_backend.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResponse {

    private final Long orderId;
    private final String paymentIntentId;
    private final OrderStatus orderStatus;
    private final String paymentStatus;
}