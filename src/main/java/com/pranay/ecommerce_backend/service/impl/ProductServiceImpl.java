package com.pranay.ecommerce_backend.service.impl;

import java.util.List;

import com.pranay.ecommerce_backend.dto.product.ProductRequest;
import com.pranay.ecommerce_backend.dto.product.ProductResponse;
import com.pranay.ecommerce_backend.entity.Product;
import com.pranay.ecommerce_backend.exception.ResourceNotFoundException;
import com.pranay.ecommerce_backend.repository.ProductRepository;
import com.pranay.ecommerce_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        log.debug("Creating product: {}", request.getName());
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .build();
        Product savedProduct = productRepository.save(product);
        log.debug("Product created with id: {}", savedProduct.getId());
        return mapToResponse(savedProduct);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        log.debug("Fetching all products");
        List<ProductResponse> products = productRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
        log.debug("Total products fetched: {}", products.size());
        return products;
    }

    @Override
    public ProductResponse getProductById(Long productId) {
        log.debug("Fetching product by id: {}", productId);
        return mapToResponse(getProductEntity(productId));
    }

    @Override
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        log.debug("Updating product id: {}", productId);
        Product product = getProductEntity(productId);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        Product updatedProduct = productRepository.save(product);
        log.debug("Product updated successfully with id: {}", updatedProduct.getId());
        return mapToResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long productId) {
        log.debug("Deleting product id: {}", productId);
        Product product = getProductEntity(productId);
        productRepository.delete(product);
        log.debug("Product deleted successfully with id: {}", productId);
    }

    private Product getProductEntity(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .build();
    }
}