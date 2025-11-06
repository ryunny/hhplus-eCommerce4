package com.hhplus.ecommerce.presentation.controller;

import com.hhplus.ecommerce.application.usecase.ProductUseCase;
import com.hhplus.ecommerce.domain.entity.Product;
import com.hhplus.ecommerce.presentation.dto.PopularProductResponse;
import com.hhplus.ecommerce.presentation.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductUseCase productUseCase;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<Product> products = productUseCase.getAllProducts();
        List<ProductResponse> response = products.stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long productId) {
        Product product = productUseCase.getProduct(productId);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productUseCase.getProductsByCategory(categoryId);
        List<ProductResponse> response = products.stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<PopularProductResponse>> getPopularProducts() {
        List<PopularProductResponse> popularProducts = productUseCase.getPopularProducts();
        return ResponseEntity.ok(popularProducts);
    }
}
