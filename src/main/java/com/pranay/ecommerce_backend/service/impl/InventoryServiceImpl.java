package com.pranay.ecommerce_backend.service.impl;

import com.pranay.ecommerce_backend.entity.Product;
import com.pranay.ecommerce_backend.exception.ValidationException;
import com.pranay.ecommerce_backend.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {

    @Override
    public void reserveStock(Product product, int quantity) {

        if (product.getStockQuantity() < quantity) {
            log.error("Insufficient stock for product: {}", product.getName());
            throw new ValidationException("Insufficient stock for product: " + product.getName());
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);

        log.debug("Stock reserved for product: {} | qty: {}", product.getName(), quantity);
    }
}
