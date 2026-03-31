package com.pranay.ecommerce_backend.service;

import com.pranay.ecommerce_backend.entity.Product;

public interface InventoryService {

    void reserveStock(Product product, int quantity);
}
