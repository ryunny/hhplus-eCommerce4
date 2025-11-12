package com.hhplus.ecommerce.infrastructure.scheduler;

import com.hhplus.ecommerce.domain.dto.ProductSalesDto;
import com.hhplus.ecommerce.domain.entity.PopularProduct;
import com.hhplus.ecommerce.domain.repository.OrderItemRepository;
import com.hhplus.ecommerce.domain.repository.PopularProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 인기 상품 갱신 스케줄러
 *
 * 주기적으로 최근 3일간 판매량을 집계하여 인기 상품 테이블을 업데이트합니다.
 * - 실행 주기: 5분마다
 * - 집계 기간: 최근 3일
 * - 저장 개수: 상위 5개
 */
@Slf4j
@Component
public class PopularProductScheduler {

    private final OrderItemRepository orderItemRepository;
    private final PopularProductRepository popularProductRepository;

    public PopularProductScheduler(OrderItemRepository orderItemRepository,
                                  PopularProductRepository popularProductRepository) {
        this.orderItemRepository = orderItemRepository;
        this.popularProductRepository = popularProductRepository;
    }

    /**
     * 인기 상품 테이블 갱신
     * 5분마다 실행됩니다.
     */
    @Scheduled(fixedDelay = 300000) // 5분 = 300,000ms
    @Transactional
    public void updatePopularProducts() {
        log.info("인기 상품 갱신 시작");

        try {
            // 1. 최근 3일간 판매량 상위 5개 조회
            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
            List<ProductSalesDto> topProducts = orderItemRepository.getTopSellingProducts(threeDaysAgo, 5);

            if (topProducts.isEmpty()) {
                log.warn("인기 상품이 없습니다. (최근 3일간 주문 없음)");
                return;
            }

            // 2. 순위별로 업데이트 또는 신규 생성
            for (int i = 0; i < topProducts.size(); i++) {
                int rank = i + 1;
                ProductSalesDto dto = topProducts.get(i);

                Optional<PopularProduct> existingOpt = popularProductRepository.findByRank(rank);

                if (existingOpt.isPresent()) {
                    // 기존 데이터 업데이트
                    PopularProduct existing = existingOpt.get();
                    existing.update(
                            dto.productId(),
                            dto.productName(),
                            dto.price(),
                            dto.totalSalesQuantity(),
                            dto.categoryName()
                    );
                    popularProductRepository.save(existing);
                    log.debug("인기 상품 업데이트: 순위={}, 상품={}, 판매량={}", rank, dto.productName(), dto.totalSalesQuantity());
                } else {
                    // 신규 생성
                    PopularProduct newPopular = new PopularProduct(
                            rank,
                            dto.productId(),
                            dto.productName(),
                            dto.price(),
                            dto.totalSalesQuantity(),
                            dto.categoryName()
                    );
                    popularProductRepository.save(newPopular);
                    log.debug("인기 상품 신규 생성: 순위={}, 상품={}, 판매량={}", rank, dto.productName(), dto.totalSalesQuantity());
                }
            }

            log.info("인기 상품 갱신 완료: {} 건", topProducts.size());

        } catch (Exception e) {
            log.error("인기 상품 갱신 실패", e);
            throw e;
        }
    }
}
