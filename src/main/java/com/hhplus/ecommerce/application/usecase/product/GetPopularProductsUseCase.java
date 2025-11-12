package com.hhplus.ecommerce.application.usecase.product;

import com.hhplus.ecommerce.domain.entity.PopularProduct;
import com.hhplus.ecommerce.domain.repository.PopularProductRepository;
import com.hhplus.ecommerce.presentation.dto.PopularProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 인기 상품 조회 UseCase
 *
 * User Story: "사용자가 인기 상품 목록을 조회한다"
 *
 * 스케줄러가 주기적으로 갱신한 인기 상품 테이블에서 데이터를 조회합니다.
 * - 복잡한 집계 쿼리 없이 단순 SELECT로 빠른 응답
 * - 최근 3일간 판매량 기준 상위 5개 (스케줄러가 5분마다 갱신)
 */
@Service
public class GetPopularProductsUseCase {

    private final PopularProductRepository popularProductRepository;

    public GetPopularProductsUseCase(PopularProductRepository popularProductRepository) {
        this.popularProductRepository = popularProductRepository;
    }

    /**
     * 인기 상품 조회
     * popular_products 테이블에서 단순 조회만 수행합니다.
     *
     * @return 인기 상품 목록 (순위 순)
     */
    @Transactional(readOnly = true)
    public List<PopularProductResponse> execute() {
        // popular_products 테이블에서 조회 (순위 오름차순)
        List<PopularProduct> popularProducts = popularProductRepository.findAllOrderByRank();

        return popularProducts.stream()
                .map(popular -> new PopularProductResponse(
                        popular.getProductId(),
                        popular.getProductName(),
                        popular.getPrice(),
                        popular.getTotalSalesQuantity(),
                        popular.getCategoryName()
                ))
                .toList();
    }
}
