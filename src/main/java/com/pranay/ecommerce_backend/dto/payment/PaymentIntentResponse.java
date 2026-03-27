package com.pranay.ecommerce_backend.dto.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentIntentResponse {

    private final Long orderId;
    private final String paymentIntentId;
    private final String clientSecret;
    private final String status;
}
