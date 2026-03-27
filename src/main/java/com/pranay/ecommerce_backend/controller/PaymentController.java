package com.pranay.ecommerce_backend.controller;

import com.pranay.ecommerce_backend.dto.payment.PaymentConfirmationRequest;
import com.pranay.ecommerce_backend.dto.payment.PaymentIntentRequest;
import com.pranay.ecommerce_backend.dto.payment.PaymentIntentResponse;
import com.pranay.ecommerce_backend.dto.payment.PaymentResponse;
import com.pranay.ecommerce_backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(@AuthenticationPrincipal UserDetails userDetails,
                                                                     @Valid @RequestBody PaymentIntentRequest request) {
        log.info("Create payment intent request by user: {} orderId: {}", userDetails.getUsername(), request.getOrderId());
        PaymentIntentResponse response = paymentService.createPaymentIntent(userDetails.getUsername(), request);
        log.info("Payment intent created successfully. paymentIntentId: {}", response.getPaymentIntentId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@AuthenticationPrincipal UserDetails userDetails,
                                                          @Valid @RequestBody PaymentConfirmationRequest request) {
        log.info("Confirm payment request by user: {} paymentIntentId: {}", userDetails.getUsername(), request.getPaymentIntentId());
        PaymentResponse response = paymentService.confirmPayment(userDetails.getUsername(), request);
        log.info("Payment confirmed successfully. orderId: {}, paymentStatus: {}", response.getOrderId(), response.getPaymentStatus());
        return ResponseEntity.ok(response);
    }
}