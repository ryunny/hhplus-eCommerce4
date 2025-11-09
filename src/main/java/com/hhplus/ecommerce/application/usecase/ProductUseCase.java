package com.hhplus.ecommerce.application.usecase;

import com.hhplus.ecommerce.domain.dto.ProductSalesDto;
import com.hhplus.ecommerce.domain.entity.Product;
import com.hhplus.ecommerce.domain.service.ProductService;
import com.hhplus.ecommerce.presentation.dto.PopularProductResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 상품 관련 유스케이스
 * ProductService를 사용하여 상품 관련 기능을 제공합니다.
 */
@Service
public class ProductUseCase {

    private final ProductService productService;

    public ProductUseCase(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 전체 상품 조회
     *
     * @return 전체 상품 목록
     */
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * 상품 단건 조회
     *
     * @param productId 상품 ID
     * @return 상품
     */
    public Product getProduct(Long productId) {
        return productService.getProduct(productId);
    }

    /**
     * 카테고리별 상품 조회
     *
     * @param categoryId 카테고리 ID
     * @return 카테고리에 속한 상품 목록
     */
    public List<Product> getProductsByCategory(Long categoryId) {
        // TODO: ProductService에 카테고리별 조회 메서드 추가 필요
        // 현재는 ProductRepository를 직접 호출하지 않으므로 제거
        throw new UnsupportedOperationException("카테고리별 조회는 Service 레이어로 이동 예정");
    }

    /**
     * 인기 상품 통계 조회 (개선됨)
     *
     * 최근 3일간 주문된 상품 중 판매량 기준 상위 5개 상품의 통계를 반환합니다.
     * Repository에서 DTO를 직접 반환하므로 중복 연산이 제거되었습니다.
     *
     * @return 인기 상품 목록 (상품 정보 + 총 판매 수량)
     */
    public List<PopularProductResponse> getPopularProducts() {
        // ProductService에서 DTO를 받아서 Response로 변환만 수행
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
