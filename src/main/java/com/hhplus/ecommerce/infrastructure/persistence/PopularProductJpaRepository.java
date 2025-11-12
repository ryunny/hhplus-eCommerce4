package com.hhplus.ecommerce.infrastructure.persistence;

import com.hhplus.ecommerce.domain.entity.PopularProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 인기 상품 JPA Repository
 */
public interface PopularProductJpaRepository extends JpaRepository<PopularProduct, Long> {

    /**
     * 순위로 조회
     */
    Optional<PopularProduct> findByRank(Integer rank);

    /**
     * 전체 조회 (순위 오름차순)
     */
    List<PopularProduct> findAllByOrderByRankAsc();
}
