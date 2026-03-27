package com.pranay.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import com.pranay.ecommerce_backend.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<CustomerOrder> findByIdAndUserId(Long id, Long userId);

    Optional<CustomerOrder> findByPaymentIntentId(String paymentIntentId);
}
