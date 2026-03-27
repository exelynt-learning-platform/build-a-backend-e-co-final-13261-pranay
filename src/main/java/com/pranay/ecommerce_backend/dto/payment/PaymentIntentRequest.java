package com.pranay.ecommerce_backend.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentIntentRequest {

    @NotNull(message = "Order id is required")
    private Long orderId;

    private String currency;
}
