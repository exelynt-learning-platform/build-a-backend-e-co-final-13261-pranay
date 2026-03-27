package com.pranay.ecommerce_backend.service;

import com.pranay.ecommerce_backend.dto.product.ProductRequest;
import com.pranay.ecommerce_backend.dto.product.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    List<ProductResponse> getAllProducts();

    ProductResponse getProductById(Long productId);

    ProductResponse updateProduct(Long productId, ProductRequest request);

    void deleteProduct(Long productId);
}
