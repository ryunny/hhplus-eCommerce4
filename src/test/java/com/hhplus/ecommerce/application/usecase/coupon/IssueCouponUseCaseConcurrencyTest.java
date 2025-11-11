package com.hhplus.ecommerce.application.usecase.coupon;

import com.hhplus.ecommerce.BaseIntegrationTest;
import com.hhplus.ecommerce.application.command.IssueCouponCommand;
import com.hhplus.ecommerce.domain.entity.Coupon;
import com.hhplus.ecommerce.domain.entity.User;
import com.hhplus.ecommerce.domain.repository.CouponRepository;
import com.hhplus.ecommerce.domain.repository.UserCouponRepository;
import com.hhplus.ecommerce.domain.repository.UserRepository;
import com.hhplus.ecommerce.domain.vo.DiscountRate;
import com.hhplus.ecommerce.domain.vo.Email;
import com.hhplus.ecommerce.domain.vo.Money;
import com.hhplus.ecommerce.domain.vo.Phone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * IssueCouponUseCase 동시성 테스트
 *
 * 실제 멀티스레드 환경에서 쿠폰 발급의 동시성 제어를 검증합니다.
 * - 선착순 쿠폰 발급 시 재고 정확성
 * - 비관적 락을 통한 동시성 제어
 * - 중복 발급 방지
 */
class IssueCouponUseCaseConcurrencyTest extends BaseIntegrationTest {

    @Autowired
    private IssueCouponUseCase issueCouponUseCase;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Test
    @DisplayName("100명이 동시에 10개 남은 쿠폰을 발급받으면 정확히 10명만 성공한다")
    void issueCoupon_Concurrency_LimitedQuantity() throws InterruptedException {
        // given - 10개 재고의 쿠폰 생성
        Coupon coupon = createPercentageCoupon("선착순 10명 쿠폰", 20, 10);
        couponRepository.save(coupon);

        // given - 100명의 사용자 생성
        int totalUsers = 100;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < totalUsers; i++) {
            User user = createUser("사용자" + i, "user" + i + "@test.com", "010-0000-" + String.format("%04d", i));
            users.add(userRepository.save(user));
        }

        // given - 동시성 제어를 위한 설정
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch readyLatch = new CountDownLatch(totalUsers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalUsers);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 100명이 동시에 쿠폰 발급 시도
        for (User user : users) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await(); // 모든 스레드가 준비될 때까지 대기

                    IssueCouponCommand command = new IssueCouponCommand(user.getPublicId(), coupon.getId());
                    issueCouponUseCase.execute(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(); // 모든 스레드가 준비될 때까지 대기
        startLatch.countDown(); // 동시에 시작
        doneLatch.await(); // 모든 스레드가 완료될 때까지 대기

        executorService.shutdown();

        // then - 정확히 10명만 성공
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failCount.get()).isEqualTo(90);

        // then - 쿠폰 발급 수량 확인
        Coupon updatedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(10);
        assertThat(updatedCoupon.getTotalQuantity()).isEqualTo(10);

        // then - 실제 발급된 UserCoupon 개수 확인
        long issuedCouponCount = userCouponRepository.findAll().stream()
                .filter(uc -> uc.getCoupon().getId().equals(coupon.getId()))
                .count();
        assertThat(issuedCouponCount).isEqualTo(10);
    }

    @Test
    @DisplayName("동일한 사용자가 여러 스레드에서 동시에 쿠폰을 발급받으려 해도 1개만 발급된다")
    void issueCoupon_Concurrency_SameUser() throws InterruptedException {
        // given - 사용자 생성
        User user = createUser("중복시도자", "duplicate@test.com", "010-1234-5678");
        userRepository.save(user);

        // given - 충분한 재고의 쿠폰 생성
        Coupon coupon = createPercentageCoupon("중복 방지 테스트 쿠폰", 10, 100);
        couponRepository.save(coupon);

        // given - 동시성 제어를 위한 설정
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 동일 사용자가 10개 스레드에서 동시 발급 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    IssueCouponCommand command = new IssueCouponCommand(user.getPublicId(), coupon.getId());
                    issueCouponUseCase.execute(command);
                    successCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    if (e.getMessage().contains("이미 발급받은 쿠폰입니다")) {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        executorService.shutdown();

        // then - 1개만 성공
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);

        // then - 해당 사용자의 쿠폰이 1개만 발급되었는지 확인
        List<com.hhplus.ecommerce.domain.entity.UserCoupon> userCoupons =
                userCouponRepository.findByUserId(user.getId());
        assertThat(userCoupons).hasSize(1);
    }

    @Test
    @DisplayName("재고가 1개 남았을 때 100명이 동시에 요청하면 1명만 성공한다")
    void issueCoupon_Concurrency_LastOne() throws InterruptedException {
        // given - 재고 1개인 쿠폰 생성
        Coupon coupon = createPercentageCoupon("마지막 1개 쿠폰", 50, 1);
        couponRepository.save(coupon);

        // given - 100명의 사용자 생성
        int totalUsers = 100;
        List<User> users = new ArrayList<>();
        for (int i = 0; i < totalUsers; i++) {
            User user = createUser("유저" + i, "testuser" + i + "@test.com", "010-9999-" + String.format("%04d", i));
            users.add(userRepository.save(user));
        }

        // given - 동시성 제어
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch readyLatch = new CountDownLatch(totalUsers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalUsers);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 100명이 동시에 마지막 1개 쿠폰 발급 시도
        for (User user : users) {
            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    IssueCouponCommand command = new IssueCouponCommand(user.getPublicId(), coupon.getId());
                    issueCouponUseCase.execute(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();

        executorService.shutdown();

        // then - 정확히 1명만 성공
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(99);

        // then - 쿠폰 발급 수량 확인
        Coupon updatedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(1);
        assertThat(updatedCoupon.getTotalQuantity()).isEqualTo(1);
    }

    // Helper methods
    private User createUser(String name, String email, String phone) {
        return new User(name, new Email(email), new Phone(phone));
    }

    private Coupon createPercentageCoupon(String name, int discountRate, int totalQuantity) {
        return new Coupon(
                name,
                "PERCENTAGE",
                new DiscountRate(discountRate),
                null,
                new Money(0L),
                totalQuantity,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30),
                false
        );
    }
}
