package com.hhplus.ecommerce.infrastructure.persistence;

import com.hhplus.ecommerce.domain.entity.PopularProduct;
import com.hhplus.ecommerce.domain.repository.PopularProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 인기 상품 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class PopularProductRepositoryImpl implements PopularProductRepository {

    private final PopularProductJpaRepository popularProductJpaRepository;

    @Override
    public PopularProduct save(PopularProduct popularProduct) {
        return popularProductJpaRepository.save(popularProduct);
    }

    @Override
    public Optional<PopularProduct> findByRank(Integer rank) {
        return popularProductJpaRepository.findByRank(rank);
    }

    @Override
    public List<PopularProduct> findAllOrderByRank() {
        return popularProductJpaRepository.findAllByOrderByRankAsc();
    }

    @Override
    public void deleteAll() {
        popularProductJpaRepository.deleteAll();
    }
}
