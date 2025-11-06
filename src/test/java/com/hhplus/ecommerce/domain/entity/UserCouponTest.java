package com.hhplus.ecommerce.domain.entity;

import com.hhplus.ecommerce.domain.enums.CouponStatus;
import com.hhplus.ecommerce.domain.vo.DiscountRate;
import com.hhplus.ecommerce.domain.vo.Email;
import com.hhplus.ecommerce.domain.vo.Money;
import com.hhplus.ecommerce.domain.vo.Phone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserCouponTest {

    @Test
    @DisplayName("미사용 쿠폰을 사용 처리한다")
    void use_Success() {
        // given
        User user = new User("홍길동", new Email("hong@test.com"), new Phone("010-1234-5678"));
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = new Coupon(
                "10% 할인 쿠폰",
                "PERCENTAGE",
                new DiscountRate(10),
                null,
                new Money(50000L),
                100,
                now.minusDays(1),
                now.plusDays(30)
        );
        UserCoupon userCoupon = new UserCoupon(user, coupon, CouponStatus.UNUSED, now.plusDays(30));

        // when
        userCoupon.use();

        // then
        assertThat(userCoupon.getStatus()).isEqualTo(CouponStatus.USED);
    }

    @Test
    @DisplayName("이미 사용된 쿠폰을 사용하려고 하면 예외가 발생한다")
    void use_AlreadyUsed() {
        // given
        User user = new User("홍길동", new Email("hong@test.com"), new Phone("010-1234-5678"));
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = new Coupon(
                "10% 할인 쿠폰",
                "PERCENTAGE",
                new DiscountRate(10),
                null,
                new Money(50000L),
                100,
                now.minusDays(1),
                now.plusDays(30)
        );
        UserCoupon userCoupon = new UserCoupon(user, coupon, CouponStatus.UNUSED, now.plusDays(30));
        userCoupon.use();

        // when & then
        assertThatThrownBy(() -> userCoupon.use())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 사용된 쿠폰입니다.");
    }

    @Test
    @DisplayName("만료된 쿠폰을 사용하려고 하면 예외가 발생한다")
    void use_Expired() {
        // given
        User user = new User("홍길동", new Email("hong@test.com"), new Phone("010-1234-5678"));
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = new Coupon(
                "만료된 쿠폰",
                "PERCENTAGE",
                new DiscountRate(10),
                null,
                new Money(50000L),
                100,
                now.minusDays(10),
                now.minusDays(5)
        );
        UserCoupon userCoupon = new UserCoupon(user, coupon, CouponStatus.UNUSED, now.minusDays(1));

        // when & then
        assertThatThrownBy(() -> userCoupon.use())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("만료된 쿠폰입니다.");
    }

    @Test
    @DisplayName("사용된 쿠폰을 취소한다")
    void cancel_Success() {
        // given
        User user = new User("홍길동", new Email("hong@test.com"), new Phone("010-1234-5678"));
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = new Coupon(
                "10% 할인 쿠폰",
                "PERCENTAGE",
                new DiscountRate(10),
                null,
                new Money(50000L),
                100,
                now.minusDays(1),
                now.plusDays(30)
        );
        UserCoupon userCoupon = new UserCoupon(user, coupon, CouponStatus.UNUSED, now.plusDays(30));
        userCoupon.use();

        // when
        userCoupon.cancel();

        // then
        assertThat(userCoupon.getStatus()).isEqualTo(CouponStatus.UNUSED);
    }

    @Test
    @DisplayName("사용되지 않은 쿠폰을 취소하려고 하면 예외가 발생한다")
    void cancel_NotUsed() {
        // given
        User user = new User("홍길동", new Email("hong@test.com"), new Phone("010-1234-5678"));
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = new Coupon(
                "10% 할인 쿠폰",
                "PERCENTAGE",
                new DiscountRate(10),
                null,
                new Money(50000L),
                100,
                now.minusDays(1),
                now.plusDays(30)
        );
        UserCoupon userCoupon = new UserCoupon(user, coupon, CouponStatus.UNUSED, now.plusDays(30));

        // when & then
        assertThatThrownBy(() -> userCoupon.cancel())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("사용된 쿠폰만 취소할 수 있습니다.");
    }

    @Test
    @DisplayName("쿠폰을 만료 처리한다")
    void expire_Success() {
        // given
        User user = new User("홍길동", new Email("hong@test.com"), new Phone("010-1234-5678"));
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = new Coupon(
                "10% 할인 쿠폰",
                "PERCENTAGE",
                new DiscountRate(10),
                null,
                new Money(50000L),
                100,
                now.minusDays(1),
                now.plusDays(30)
        );
        UserCoupon userCoupon = new UserCoupon(user, coupon, CouponStatus.UNUSED, now.plusDays(30));

        // when
        userCoupon.expire();

        // then
        assertThat(userCoupon.getStatus()).isEqualTo(CouponStatus.EXPIRED);
    }
}
