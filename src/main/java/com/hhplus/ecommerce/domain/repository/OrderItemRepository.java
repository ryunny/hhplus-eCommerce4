package com.hhplus.ecommerce.domain.repository;

import com.hhplus.ecommerce.domain.dto.ProductSalesDto;
import com.hhplus.ecommerce.domain.entity.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository {
    OrderItem save(OrderItem orderItem);
    Optional<OrderItem> findById(Long id);
    List<OrderItem> findByOrderId(Long orderId);
    void deleteById(Long id);

    /**
     * @deprecated 중복 연산이 발생하므로 getTopSellingProducts() 사용 권장
     */
    @Deprecated
    List<OrderItem> getOrderItemByTopFive();

    /**
     * 인기 상품 Top N 조회 (개선된 메서드)
     * 최근 3일간 판매량 기준 상위 N개 상품의 통계를 반환합니다.
     *
     * @param limit 조회할 상품 개수
     * @return 상품 판매 통계 DTO 목록
     */
    List<ProductSalesDto> getTopSellingProducts(int limit);
}
