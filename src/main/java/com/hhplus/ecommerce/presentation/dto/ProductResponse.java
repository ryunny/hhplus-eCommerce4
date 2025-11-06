package com.hhplus.ecommerce.presentation.dto;

import com.hhplus.ecommerce.domain.entity.Product;

public record ProductResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        Long price,
        Integer stock
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getDescription(),
                product.getPrice().getAmount(),
                product.getStock().getQuantity()
        );
    }
}
