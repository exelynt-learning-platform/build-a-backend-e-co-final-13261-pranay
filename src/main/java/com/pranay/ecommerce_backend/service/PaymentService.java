package com.pranay.ecommerce_backend.service;

import com.pranay.ecommerce_backend.dto.payment.PaymentConfirmationRequest;
import com.pranay.ecommerce_backend.dto.payment.PaymentIntentRequest;
import com.pranay.ecommerce_backend.dto.payment.PaymentIntentResponse;
import com.pranay.ecommerce_backend.dto.payment.PaymentResponse;

public interface PaymentService {

    PaymentIntentResponse createPaymentIntent(String userEmail, PaymentIntentRequest request);

    PaymentResponse confirmPayment(String userEmail, PaymentConfirmationRequest request);
}
