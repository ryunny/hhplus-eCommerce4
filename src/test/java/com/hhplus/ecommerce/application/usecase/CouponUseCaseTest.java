package com.hhplus.ecommerce.application.usecase;

import com.hhplus.ecommerce.domain.entity.Coupon;
import com.hhplus.ecommerce.domain.entity.User;
import com.hhplus.ecommerce.domain.entity.UserCoupon;
import com.hhplus.ecommerce.domain.enums.CouponStatus;
import com.hhplus.ecommerce.test.mocks.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponUseCaseTest {

    private CouponUseCase couponUseCase;
    private MockCouponRepository couponRepository;
    private MockUserCouponRepository userCouponRepository;
    private MockUserRepository userRepository;
    private MockCouponQueueRepository couponQueueRepository;

    @BeforeEach
    void setUp() {
        couponRepository = new MockCouponRepository();
        userCouponRepository = new MockUserCouponRepository();
        userRepository = new MockUserRepository();
        couponQueueRepository = new MockCouponQueueRepository();

        couponUseCase = new CouponUseCase(
                couponRepository,
                userCouponRepository,
                userRepository,
                couponQueueRepository
        );
    }

    @Test
    @DisplayName("발급 가능한 쿠폰 목록을 조회한다")
    void getIssuableCoupons() {
        // when
        List<Coupon> issuableCoupons = couponUseCase.getIssuableCoupons();

        // then
        assertThat(issuableCoupons).hasSize(2); // 만료된 쿠폰 제외
        assertThat(issuableCoupons).allMatch(Coupon::isIssuable);
    }

    @Test
    @DisplayName("쿠폰을 성공적으로 발급한다")
    void issueCoupon_Success() {
        // given
        Long userId = 1L; // MockUserRepository의 초기 데이터
        Long couponId = 1L; // MockCouponRepository의 초기 데이터

        // when
        UserCoupon userCoupon = couponUseCase.issueCoupon(userId, couponId);

        // then
        assertThat(userCoupon).isNotNull();
        assertThat(userCoupon.getUser().getId()).isEqualTo(userId);
        assertThat(userCoupon.getCoupon().getId()).isEqualTo(couponId);
        assertThat(userCoupon.getStatus()).isEqualTo(CouponStatus.UNUSED);

        // 쿠폰 발급 수량 증가 확인
        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("이미 발급받은 쿠폰을 다시 발급받으려고 하면 예외가 발생한다")
    void issueCoupon_AlreadyIssued() {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        couponUseCase.issueCoupon(userId, couponId); // 첫 번째 발급

        // when & then
        assertThatThrownBy(() -> couponUseCase.issueCoupon(userId, couponId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 발급받은 쿠폰입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 사용자가 쿠폰을 발급받으려고 하면 예외가 발생한다")
    void issueCoupon_UserNotFound() {
        // given
        Long invalidUserId = 999L;
        Long couponId = 1L;

        // when & then
        assertThatThrownBy(() -> couponUseCase.issueCoupon(invalidUserId, couponId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰을 발급받으려고 하면 예외가 발생한다")
    void issueCoupon_CouponNotFound() {
        // given
        Long userId = 1L;
        Long invalidCouponId = 999L;

        // when & then
        assertThatThrownBy(() -> couponUseCase.issueCoupon(userId, invalidCouponId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("쿠폰을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자가 보유한 쿠폰 목록을 조회한다")
    void getUserCoupons() {
        // given
        Long userId = 1L;
        Long couponId1 = 1L;
        Long couponId2 = 2L;

        couponUseCase.issueCoupon(userId, couponId1);
        couponUseCase.issueCoupon(userId, couponId2);

        // when
        List<UserCoupon> userCoupons = couponUseCase.getUserCoupons(userId);

        // then
        assertThat(userCoupons).hasSize(2);
        assertThat(userCoupons).allMatch(uc -> uc.getUser().getId().equals(userId));
    }

    @Test
    @DisplayName("사용자가 사용 가능한 쿠폰 목록을 조회한다")
    void getAvailableCoupons() {
        // given
        Long userId = 1L;
        Long couponId1 = 1L;
        Long couponId2 = 2L;

        UserCoupon userCoupon1 = couponUseCase.issueCoupon(userId, couponId1);
        UserCoupon userCoupon2 = couponUseCase.issueCoupon(userId, couponId2);

        // 첫 번째 쿠폰 사용 처리
        userCoupon1.use();
        userCouponRepository.save(userCoupon1);

        // when
        List<UserCoupon> availableCoupons = couponUseCase.getAvailableCoupons(userId);

        // then
        assertThat(availableCoupons).hasSize(1);
        assertThat(availableCoupons.get(0).getId()).isEqualTo(userCoupon2.getId());
        assertThat(availableCoupons).allMatch(uc -> uc.getStatus() == CouponStatus.UNUSED);
    }

    @Test
    @DisplayName("만료된 쿠폰을 자동으로 만료 처리한다")
    void expireOldCoupons() {
        // given
        LocalDateTime now = LocalDateTime.now();
        User user = userRepository.findById(1L).orElseThrow();
        Coupon coupon = couponRepository.findById(1L).orElseThrow();

        // 만료된 UserCoupon 생성
        UserCoupon expiredUserCoupon = userCouponRepository.createUserCoupon(
                1L,
                user,
                coupon,
                CouponStatus.UNUSED,
                now.minusDays(1) // 어제 만료
        );
        userCouponRepository.save(expiredUserCoupon);
        coupon.increaseIssuedQuantity();
        couponRepository.save(coupon);

        // when
        int expiredCount = couponUseCase.expireOldCoupons();

        // then
        assertThat(expiredCount).isEqualTo(1);

        UserCoupon updated = userCouponRepository.findById(1L).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(CouponStatus.EXPIRED);

        Coupon updatedCoupon = couponRepository.findById(1L).orElseThrow();
        assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(0);
    }
}
