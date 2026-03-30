package com.pranay.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import com.pranay.ecommerce_backend.entity.CustomerOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<CustomerOrder> findByIdAndUserId(Long id, Long userId);

    Optional<CustomerOrder> findByPaymentIntentId(String paymentIntentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM CustomerOrder o WHERE o.paymentIntentId = :paymentIntentId")
    Optional<CustomerOrder> findByPaymentIntentIdForUpdate(@Param("paymentIntentId") String paymentIntentId);
}
