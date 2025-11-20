package com.hhplus.ecommerce.infrastructure.persistence;

import com.hhplus.ecommerce.domain.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    /**
     * 조건부 업데이트: 재고가 충분할 때만 차감
     * @return 업데이트된 행의 개수 (1: 성공, 0: 실패)
     */
    @Modifying
    @Query(value = "UPDATE products SET stock = stock - :quantity " +
           "WHERE id = :id AND stock >= :quantity", nativeQuery = true)
    int decreaseStockConditionally(@Param("id") Long id, @Param("quantity") int quantity);
}
