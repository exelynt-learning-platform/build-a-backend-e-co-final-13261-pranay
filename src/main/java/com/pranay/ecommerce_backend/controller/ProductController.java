package com.pranay.ecommerce_backend.controller;

import com.pranay.ecommerce_backend.dto.product.ProductRequest;
import com.pranay.ecommerce_backend.dto.product.ProductResponse;
import com.pranay.ecommerce_backend.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        log.info("Create product request received: {}", request.getName());
        ProductResponse response = productService.createProduct(request);
        log.info("Product created successfully with id: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("Get all products request received");
        List<ProductResponse> products = productService.getAllProducts();
        log.info("Fetched {} products", products.size());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        log.info("Get product request received for id: {}", productId);
        ProductResponse response = productService.getProductById(productId);
        log.info("Fetched product: {} with id: {}", response.getName(), response.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long productId,
                                                         @Valid @RequestBody ProductRequest request) {
        log.info("Update product request received for id: {}", productId);
        ProductResponse response = productService.updateProduct(productId, request);
        log.info("Product updated successfully with id: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        log.info("Delete product request received for id: {}", productId);
        productService.deleteProduct(productId);
        log.info("Product deleted successfully with id: {}", productId);
        return ResponseEntity.noContent().build();
    }
}