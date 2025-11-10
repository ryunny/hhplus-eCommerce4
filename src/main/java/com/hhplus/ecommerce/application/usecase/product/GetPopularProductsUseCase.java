package com.hhplus.ecommerce.application.usecase.product;

import com.hhplus.ecommerce.domain.dto.ProductSalesDto;
import com.hhplus.ecommerce.domain.service.ProductService;
import com.hhplus.ecommerce.presentation.dto.PopularProductResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 인기 상품 조회 UseCase
 *
 * User Story: "사용자가 인기 상품 목록을 조회한다"
 *
 * 최근 3일간 주문된 상품 중 판매량 기준 상위 5개 상품의 통계를 반환합니다.
 */
@Service
public class GetPopularProductsUseCase {

    private final ProductService productService;

    public GetPopularProductsUseCase(ProductService productService) {
        this.productService = productService;
    }

    public List<PopularProductResponse> execute() {
        // ProductService에서 DTO를 받아서 Response로 변환
        List<ProductSalesDto> salesDtos = productService.getTopSellingProducts(5);

        return salesDtos.stream()
                .map(dto -> new PopularProductResponse(
                        dto.productId(),
                        dto.productName(),
                        dto.price(),
                        dto.totalSalesQuantity(),
                        dto.categoryName()
                ))
                .toList();
    }
}
