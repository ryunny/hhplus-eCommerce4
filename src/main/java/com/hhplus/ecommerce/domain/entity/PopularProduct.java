package com.hhplus.ecommerce.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 인기 상품 스냅샷 엔티티
 *
 * 스케줄러가 주기적으로 집계한 인기 상품 정보를 저장합니다.
 * - 최근 3일간 판매량 기준 상위 5개 상품
 * - 5분마다 자동 갱신
 */
@Entity
@Table(name = "popular_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 순위 (1~5)
     * 중복 순위가 없도록 유니크 제약조건 설정
     */
    @Column(nullable = false, unique = true)
    private Integer rank;

    /**
     * 상품 ID
     */
    @Column(nullable = false)
    private Long productId;

    /**
     * 상품명 (스냅샷)
     */
    @Column(nullable = false, length = 200)
    private String productName;

    /**
     * 가격 (스냅샷)
     */
    @Column(nullable = false)
    private Long price;

    /**
     * 총 판매 수량 (집계 기간 내)
     */
    @Column(nullable = false)
    private Integer totalSalesQuantity;

    /**
     * 카테고리명 (스냅샷)
     */
    @Column(nullable = false, length = 100)
    private String categoryName;

    /**
     * 마지막 업데이트 시각
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 생성자 (신규 또는 업데이트용)
     */
    public PopularProduct(Integer rank, Long productId, String productName, Long price,
                         Integer totalSalesQuantity, String categoryName) {
        this.rank = rank;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.totalSalesQuantity = totalSalesQuantity;
        this.categoryName = categoryName;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 정보 업데이트
     */
    public void update(Long productId, String productName, Long price,
                      Integer totalSalesQuantity, String categoryName) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.totalSalesQuantity = totalSalesQuantity;
        this.categoryName = categoryName;
        this.updatedAt = LocalDateTime.now();
    }
}
