package com.pranay.ecommerce_backend.service.impl;

import com.pranay.ecommerce_backend.dto.payment.PaymentConfirmationRequest;
import com.pranay.ecommerce_backend.dto.payment.PaymentIntentRequest;
import com.pranay.ecommerce_backend.dto.payment.PaymentIntentResponse;
import com.pranay.ecommerce_backend.dto.payment.PaymentResponse;
import com.pranay.ecommerce_backend.entity.CustomerOrder;
import com.pranay.ecommerce_backend.entity.OrderStatus;
import com.pranay.ecommerce_backend.entity.User;
import com.pranay.ecommerce_backend.exception.ResourceNotFoundException;
import com.pranay.ecommerce_backend.exception.ValidationException;
import com.pranay.ecommerce_backend.repository.OrderRepository;
import com.pranay.ecommerce_backend.repository.UserRepository;
import com.pranay.ecommerce_backend.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String PAYMENT_STATUS_SUCCEEDED = "succeeded";
    private static final String PAYMENT_STATUS_PROCESSING = "processing";
    private static final String PAYMENT_STATUS_REQUIRES_CAPTURE = "requires_capture";

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Value("${stripe.currency}")
    private String currency;

    @Override
    @Transactional
    public PaymentIntentResponse createPaymentIntent(String userEmail, PaymentIntentRequest request) {
        CustomerOrder order = getOwnedOrder(userEmail, request.getOrderId());
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.FAILED) {
            throw new ValidationException("Payment cannot be created for order status: " + order.getStatus());
        }

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(toMinorUnits(order.getTotalPrice()))
                    .setCurrency(resolveCurrency(request))
                    .putMetadata("orderId", String.valueOf(order.getId()))
                    .putMetadata("userId", String.valueOf(order.getUser().getId()))
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            order.setPaymentIntentId(paymentIntent.getId());
            orderRepository.save(order);

            return PaymentIntentResponse.builder()
                    .orderId(order.getId())
                    .paymentIntentId(paymentIntent.getId())
                    .clientSecret(paymentIntent.getClientSecret())
                    .status(paymentIntent.getStatus())
                    .build();
        } catch (StripeException ex) {
            throw new ValidationException("Unable to create payment intent: " + ex.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentResponse confirmPayment(String userEmail, PaymentConfirmationRequest request) {
        CustomerOrder order = orderRepository.findByPaymentIntentId(request.getPaymentIntentId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for payment intent"));

        validateUserAccess(userEmail, order);

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(request.getPaymentIntentId());
            String paymentStatus = paymentIntent.getStatus();
            order.setStatus(mapOrderStatus(paymentStatus));
            orderRepository.save(order);

            return PaymentResponse.builder()
                    .orderId(order.getId())
                    .paymentIntentId(paymentIntent.getId())
                    .orderStatus(order.getStatus())
                    .paymentStatus(paymentStatus)
                    .build();
        } catch (StripeException ex) {
            throw new ValidationException("Unable to confirm payment: " + ex.getMessage());
        }
    }

    private CustomerOrder getOwnedOrder(String userEmail, Long orderId) {
        User user = getUser(userEmail);
        return orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    private void validateUserAccess(String userEmail, CustomerOrder order) {
        User user = getUser(userEmail);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ValidationException("You can only access your own payments");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Long toMinorUnits(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private String resolveCurrency(PaymentIntentRequest request) {
        if (StringUtils.hasText(request.getCurrency())) {
            return request.getCurrency().trim().toLowerCase(Locale.ROOT);
        }
        return currency.toLowerCase(Locale.ROOT);
    }

    private OrderStatus mapOrderStatus(String paymentStatus) {
        if (PAYMENT_STATUS_SUCCEEDED.equalsIgnoreCase(paymentStatus)) {
            return OrderStatus.PAID;
        }
        if (PAYMENT_STATUS_PROCESSING.equalsIgnoreCase(paymentStatus)
                || PAYMENT_STATUS_REQUIRES_CAPTURE.equalsIgnoreCase(paymentStatus)) {
            return OrderStatus.PENDING;
        }
        return OrderStatus.FAILED;
    }
}
