package com.hhplus.ecommerce.application.usecase;

import com.hhplus.ecommerce.domain.entity.Coupon;
import com.hhplus.ecommerce.domain.entity.User;
import com.hhplus.ecommerce.domain.vo.DiscountRate;
import com.hhplus.ecommerce.domain.vo.Email;
import com.hhplus.ecommerce.domain.vo.Money;
import com.hhplus.ecommerce.domain.vo.Phone;
import com.hhplus.ecommerce.test.mocks.MockCouponQueueRepository;
import com.hhplus.ecommerce.test.mocks.MockCouponRepository;
import com.hhplus.ecommerce.test.mocks.MockUserCouponRepository;
import com.hhplus.ecommerce.test.mocks.MockUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CouponConcurrencyTest {

    @Test
    @DisplayName("100명이 동시에 10개 한정 쿠폰을 발급받으려고 할 때 정확히 10명만 발급받는다")
    void issueCoupon_Concurrency_LimitedQuantity() throws InterruptedException {
        // given
        MockCouponRepository couponRepository = new MockCouponRepository();
        MockUserCouponRepository userCouponRepository = new MockUserCouponRepository();
        MockUserRepository userRepository = new MockUserRepository();
        MockCouponQueueRepository couponQueueRepository = new MockCouponQueueRepository();

        CouponUseCase couponUseCase = new CouponUseCase(
                couponRepository,
                userCouponRepository,
                userRepository,
                couponQueueRepository
        );

        // 한정 수량 쿠폰 생성 (10개 한정)
        LocalDateTime now = LocalDateTime.now();
        Coupon limitedCoupon = new Coupon(
                "한정 쿠폰",
                "PERCENTAGE",
                new DiscountRate(20),
                null,
                new Money(10000L),
                10, // 총 10개만 발급 가능
                now.minusDays(1),
                now.plusDays(30)
        );
        Coupon savedCoupon = couponRepository.save(limitedCoupon);

        // 100명의 사용자 생성
        for (int i = 1; i <= 100; i++) {
            User user = new User("User" + i, new Email("user" + i + "@test.com"), new Phone("010-0000-" + String.format("%04d", i)));
            userRepository.save(user);
        }

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 100명이 동시에 쿠폰 발급 시도
        for (int i = 1; i <= threadCount; i++) {
            final long userId = i + 3L; // MockUserRepository의 초기 데이터 3개 이후부터
            executorService.submit(() -> {
                try {
                    couponUseCase.issueCoupon(userId, savedCoupon.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(10); // 정확히 10명만 성공
        assertThat(failCount.get()).isEqualTo(90); // 나머지 90명은 실패

        // 쿠폰 발급 수량 확인
        Coupon updatedCoupon = couponRepository.findById(savedCoupon.getId()).orElseThrow();
        assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(10);
        assertThat(updatedCoupon.isIssuable()).isFalse(); // 더 이상 발급 불가
    }

    @Test
    @DisplayName("동일한 사용자가 여러 스레드에서 동시에 같은 쿠폰을 발급받으려고 할 때 1번만 발급된다")
    void issueCoupon_Concurrency_SameUser() throws InterruptedException {
        // given
        MockCouponRepository couponRepository = new MockCouponRepository();
        MockUserCouponRepository userCouponRepository = new MockUserCouponRepository();
        MockUserRepository userRepository = new MockUserRepository();
        MockCouponQueueRepository couponQueueRepository = new MockCouponQueueRepository();

        CouponUseCase couponUseCase = new CouponUseCase(
                couponRepository,
                userCouponRepository,
                userRepository,
                couponQueueRepository
        );

        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = new Coupon(
                "일반 쿠폰",
                "PERCENTAGE",
                new DiscountRate(10),
                null,
                new Money(10000L),
                100,
                now.minusDays(1),
                now.plusDays(30)
        );
        Coupon savedCoupon = couponRepository.save(coupon);

        Long userId = 1L; // 동일한 사용자

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 동일한 사용자가 10개 스레드에서 동시에 발급 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    couponUseCase.issueCoupon(userId, savedCoupon.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1); // 1번만 성공
        assertThat(failCount.get()).isEqualTo(9); // 나머지 9번은 실패

        // 쿠폰 발급 수량 확인
        Coupon updatedCoupon = couponRepository.findById(savedCoupon.getId()).orElseThrow();
        assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("여러 사용자가 서로 다른 쿠폰을 동시에 발급받을 때 모두 성공한다")
    void issueCoupon_Concurrency_DifferentCoupons() throws InterruptedException {
        // given
        MockCouponRepository couponRepository = new MockCouponRepository();
        MockUserCouponRepository userCouponRepository = new MockUserCouponRepository();
        MockUserRepository userRepository = new MockUserRepository();
        MockCouponQueueRepository couponQueueRepository = new MockCouponQueueRepository();

        CouponUseCase couponUseCase = new CouponUseCase(
                couponRepository,
                userCouponRepository,
                userRepository,
                couponQueueRepository
        );

        // 쿠폰 2개 사용 (MockCouponRepository의 초기 데이터)
        Long couponId1 = 1L;
        Long couponId2 = 2L;

        // 사용자 2명 (MockUserRepository의 초기 데이터)
        Long userId1 = 1L;
        Long userId2 = 2L;

        int threadCount = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        // when - 각 사용자가 각 쿠폰을 동시에 발급받으려고 시도
        executorService.submit(() -> {
            try {
                couponUseCase.issueCoupon(userId1, couponId1);
                successCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                couponUseCase.issueCoupon(userId1, couponId2);
                successCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                couponUseCase.issueCoupon(userId2, couponId1);
                successCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                couponUseCase.issueCoupon(userId2, couponId2);
                successCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(4); // 모두 성공

        // 각 쿠폰의 발급 수량 확인
        Coupon coupon1 = couponRepository.findById(couponId1).orElseThrow();
        Coupon coupon2 = couponRepository.findById(couponId2).orElseThrow();

        assertThat(coupon1.getIssuedQuantity()).isEqualTo(2); // 2명이 발급
        assertThat(coupon2.getIssuedQuantity()).isEqualTo(2); // 2명이 발급
    }
}
