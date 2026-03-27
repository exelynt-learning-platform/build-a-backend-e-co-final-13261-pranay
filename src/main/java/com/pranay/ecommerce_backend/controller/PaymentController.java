package com.pranay.ecommerce_backend.controller;

import com.pranay.ecommerce_backend.dto.payment.PaymentConfirmationRequest;
import com.pranay.ecommerce_backend.dto.payment.PaymentIntentRequest;
import com.pranay.ecommerce_backend.dto.payment.PaymentIntentResponse;
import com.pranay.ecommerce_backend.dto.payment.PaymentResponse;
import com.pranay.ecommerce_backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(@AuthenticationPrincipal UserDetails userDetails,
                                                                     @Valid @RequestBody PaymentIntentRequest request) {
        return ResponseEntity.ok(paymentService.createPaymentIntent(userDetails.getUsername(), request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@AuthenticationPrincipal UserDetails userDetails,
                                                          @Valid @RequestBody PaymentConfirmationRequest request) {
        return ResponseEntity.ok(paymentService.confirmPayment(userDetails.getUsername(), request));
    }
}
