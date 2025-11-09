package com.hhplus.ecommerce.domain.dto;

import com.hhplus.ecommerce.domain.entity.Product;

/**
 * 상품 판매 통계 DTO
 * Repository와 Service 레이어 간 데이터 전달용
 */
public record ProductSalesDto(
        Long productId,
        String productName,
        Long price,
        int totalSalesQuantity,
        String categoryName
) {
    public static ProductSalesDto of(Product product, int totalSalesQuantity) {
        return new ProductSalesDto(
                product.getId(),
                product.getName(),
                product.getPrice().getAmount(),
                totalSalesQuantity,
                product.getCategory().getName()
        );
    }
}
