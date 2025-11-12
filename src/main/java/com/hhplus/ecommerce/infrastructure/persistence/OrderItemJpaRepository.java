package com.hhplus.ecommerce.infrastructure.persistence;

import com.hhplus.ecommerce.domain.dto.ProductSalesDto;
import com.hhplus.ecommerce.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * 인기 상품 조회 (Native Query)
     * 최근 3일간 판매량 기준 상위 N개 상품 통계
     */
    @Query(value = "SELECT oi.product_id AS productId, " +
                   "p.name AS productName, " +
                   "p.price AS price, " +
                   "SUM(oi.quantity) AS totalSalesQuantity, " +
                   "c.name AS categoryName " +
                   "FROM order_items oi " +
                   "JOIN products p ON oi.product_id = p.id " +
                   "JOIN categories c ON p.category_id = c.id " +
                   "JOIN orders o ON oi.order_id = o.id " +
                   "WHERE o.created_at >= :threeDaysAgo " +
                   "GROUP BY oi.product_id, p.name, p.price, c.name " +
                   "ORDER BY SUM(oi.quantity) DESC " +
                   "LIMIT :limit",
           nativeQuery = true)
    List<Object[]> getTopSellingProducts(@Param("threeDaysAgo") LocalDateTime threeDaysAgo, @Param("limit") int limit);
}
