package com.hhplus.ecommerce.concurrency;

import com.hhplus.ecommerce.BaseIntegrationTest;
import com.hhplus.ecommerce.domain.entity.Category;
import com.hhplus.ecommerce.domain.entity.Product;
import com.hhplus.ecommerce.domain.repository.CategoryRepository;
import com.hhplus.ecommerce.domain.repository.ProductRepository;
import com.hhplus.ecommerce.domain.vo.Money;
import com.hhplus.ecommerce.domain.vo.Quantity;
import com.hhplus.ecommerce.domain.vo.Stock;
import com.hhplus.ecommerce.infrastructure.persistence.ProductJpaRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 락 메커니즘 비교 통합 테스트
 *
 * 1. 비관적 락 (Pessimistic Lock - PESSIMISTIC_WRITE)
 * 2. 낙관적 락 (Optimistic Lock - @Version)
 *
 * 두 락 메커니즘의 동시성 제어 정확성, 성능, 실패율을 비교합니다.
 */
class LockMechanismComparisonTest extends BaseIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private static final int TOTAL_THREADS = 100;
    private static final int INITIAL_STOCK = 10;
    private static final int PURCHASE_QUANTITY = 1;

    /**
     * 테스트 1: 비관적 락 (PESSIMISTIC_WRITE)
     * - SELECT ... FOR UPDATE로 행을 잠금
     * - 다른 트랜잭션은 락이 해제될 때까지 대기
     * - 정확성 보장, 대기 시간 발생
     */
    @Test
    @DisplayName("비관적 락: 100명이 재고 10개 상품 구매 시 정확히 10명만 성공")
    void pessimisticLock_Concurrency() throws InterruptedException {
        // given
        Category category = categoryRepository.save(new Category("전자제품"));
        Product product = createProduct(category, "비관적락 상품", 10000L, INITIAL_STOCK);
        Product savedProduct = productRepository.save(product);

        ExecutorService executorService = Executors.newFixedThreadPool(TOTAL_THREADS);
        CountDownLatch readyLatch = new CountDownLatch(TOTAL_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(TOTAL_THREADS);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicLong totalExecutionTime = new AtomicLong(0);

        // when
        for (int i = 0; i < TOTAL_THREADS; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    long startTime = System.currentTimeMillis();

                    // 비관적 락 사용 (트랜잭션 내에서)
                    transactionTemplate.execute(status -> {
                        Product lockedProduct = productJpaRepository.findByIdWithLock(savedProduct.getId())
                                .orElseThrow();
                        lockedProduct.decreaseStock(new Quantity(PURCHASE_QUANTITY));
                        productRepository.save(lockedProduct);
                        return null;
                    });

                    long endTime = System.currentTimeMillis();
                    totalExecutionTime.addAndGet(endTime - startTime);

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        long testStartTime = System.currentTimeMillis();
        startLatch.countDown();
        doneLatch.await();
        long testEndTime = System.currentTimeMillis();

        executorService.shutdown();

        // then
        Product finalProduct = productRepository.findById(savedProduct.getId()).orElseThrow();

        System.out.println("\n========== 비관적 락 테스트 결과 ==========");
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());
        System.out.println("최종 재고: " + finalProduct.getStock().getQuantity());
        System.out.println("전체 실행 시간: " + (testEndTime - testStartTime) + "ms");
        if (successCount.get() > 0) {
            System.out.println("평균 트랜잭션 시간: " + (totalExecutionTime.get() / successCount.get()) + "ms");
        }
        System.out.println("=========================================\n");

        assertThat(successCount.get()).isEqualTo(INITIAL_STOCK);
        assertThat(failCount.get()).isEqualTo(TOTAL_THREADS - INITIAL_STOCK);
        assertThat(finalProduct.getStock().getQuantity()).isEqualTo(0);
    }

    /**
     * 테스트 2: 낙관적 락 (@Version)
     * - version 필드로 충돌 감지
     * - 커밋 시점에 version이 다르면 OptimisticLockException 발생
     * - 재시도 로직 필요, 경합이 적을 때 유리
     */
    @Test
    @DisplayName("낙관적 락: 100명이 재고 10개 상품 구매 시 재시도 없이는 충돌 다수 발생")
    void optimisticLock_Concurrency_WithoutRetry() throws InterruptedException {
        // given
        Category category = categoryRepository.save(new Category("전자제품"));
        Product product = createProduct(category, "낙관적락 상품", 10000L, INITIAL_STOCK);
        Product savedProduct = productRepository.save(product);

        ExecutorService executorService = Executors.newFixedThreadPool(TOTAL_THREADS);
        CountDownLatch readyLatch = new CountDownLatch(TOTAL_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(TOTAL_THREADS);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger optimisticLockFailCount = new AtomicInteger(0);
        AtomicInteger stockShortageCount = new AtomicInteger(0);
        AtomicLong totalExecutionTime = new AtomicLong(0);

        // when
        for (int i = 0; i < TOTAL_THREADS; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    long startTime = System.currentTimeMillis();

                    // 낙관적 락 (트랜잭션 내에서 @Version 자동 체크)
                    transactionTemplate.execute(status -> {
                        Product targetProduct = productRepository.findById(savedProduct.getId())
                                .orElseThrow();
                        targetProduct.decreaseStock(new Quantity(PURCHASE_QUANTITY));
                        productRepository.save(targetProduct); // 여기서 version 체크
                        return null;
                    });

                    long endTime = System.currentTimeMillis();
                    totalExecutionTime.addAndGet(endTime - startTime);

                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                    optimisticLockFailCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    stockShortageCount.incrementAndGet();
                } catch (Exception e) {
                    optimisticLockFailCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        long testStartTime = System.currentTimeMillis();
        startLatch.countDown();
        doneLatch.await();
        long testEndTime = System.currentTimeMillis();

        executorService.shutdown();

        // then
        Product finalProduct = productRepository.findById(savedProduct.getId()).orElseThrow();

        System.out.println("\n========== 낙관적 락 테스트 결과 (재시도 없음) ==========");
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("낙관적 락 충돌 횟수: " + optimisticLockFailCount.get());
        System.out.println("재고 부족 실패 횟수: " + stockShortageCount.get());
        System.out.println("최종 재고: " + finalProduct.getStock().getQuantity());
        System.out.println("전체 실행 시간: " + (testEndTime - testStartTime) + "ms");
        if (successCount.get() > 0) {
            System.out.println("평균 트랜잭션 시간: " + (totalExecutionTime.get() / successCount.get()) + "ms");
        }
        System.out.println("=========================================\n");

        // 낙관적 락은 충돌이 많이 발생하므로 성공 횟수가 적을 수 있음
        assertThat(finalProduct.getStock().getQuantity()).isEqualTo(INITIAL_STOCK - successCount.get());
    }

    private Product createProduct(Category category, String name, Long price, int stock) {
        return new Product(category, name, "상품 설명", new Money(price), new Stock(stock));
    }
}
