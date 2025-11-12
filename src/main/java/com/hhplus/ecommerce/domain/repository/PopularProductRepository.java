package com.hhplus.ecommerce.domain.repository;

import com.hhplus.ecommerce.domain.entity.PopularProduct;

import java.util.List;
import java.util.Optional;

/**
 * 인기 상품 Repository (Domain Layer)
 */
public interface PopularProductRepository {

    /**
     * 인기 상품 저장
     */
    PopularProduct save(PopularProduct popularProduct);

    /**
     * 순위로 조회
     */
    Optional<PopularProduct> findByRank(Integer rank);

    /**
     * 전체 인기 상품 조회 (순위 오름차순)
     */
    List<PopularProduct> findAllOrderByRank();

    /**
     * 전체 삭제
     */
    void deleteAll();
}
