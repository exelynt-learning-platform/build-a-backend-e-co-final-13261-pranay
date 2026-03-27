package com.pranay.ecommerce_backend.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
}
