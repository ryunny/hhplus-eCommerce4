package com.hhplus.ecommerce.presentation.dto;

import com.hhplus.ecommerce.domain.entity.Product;

public record PopularProductResponse(
        Long productId,
        String productName,
        Long price,
        Integer totalSalesQuantity,
        String categoryName
) {
    public static PopularProductResponse of(Product product, Integer totalSalesQuantity) {
        return new PopularProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice().getAmount(),
                totalSalesQuantity,
                product.getCategory().getName()
        );
    }
}
