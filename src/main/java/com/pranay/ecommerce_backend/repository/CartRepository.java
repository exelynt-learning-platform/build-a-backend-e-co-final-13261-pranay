package com.pranay.ecommerce_backend.repository;

import java.util.Optional;

import com.pranay.ecommerce_backend.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Cart> findDetailedByUserId(Long userId);
}
