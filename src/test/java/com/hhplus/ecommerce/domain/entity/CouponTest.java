package com.hhplus.ecommerce.domain.entity;

import com.hhplus.ecommerce.domain.vo.DiscountRate;
import com.hhplus.ecommerce.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    @Test
    @DisplayName("발급 가능한 쿠폰인지 확인한다 - 발급 가능")
    void isIssuable_True() {
        // given
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

        // when
        boolean issuable = coupon.isIssuable();

        // then
        assertThat(issuable).isTrue();
    }

    @Test
    @DisplayName("발급 가능한 쿠폰인지 확인한다 - 기간 만료")
    void isIssuable_Expired() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = new Coupon(
                "만료된 쿠폰",
                "PERCENTAGE",
                new DiscountRate(10),
                null,
                new Money(50000L),
                100,
                now.minusDays(10),
                now.minusDays(1)
        );

        // when
        boolean issuable = coupon.isIssuable();

        // then
        assertThat(issuable).isFalse();
    }

    @Test
    @DisplayName("발급 가능한 쿠폰인지 확인한다 - 수량 소진")
    void isIssuable_SoldOut() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = new Coupon(
                "매진된 쿠폰",
                "PERCENTAGE",
                new DiscountRate(10),
                null,
                new Money(50000L),
                10,
                now.minusDays(1),
                now.plusDays(30)
        );

        // when
        for (int i = 0; i < 10; i++) {
            coupon.increaseIssuedQuantity();
        }
        boolean issuable = coupon.isIssuable();

        // then
        assertThat(issuable).isFalse();
        assertThat(coupon.getIssuedQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("발급 수량을 증가시킨다")
    void increaseIssuedQuantity_Success() {
        // given
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

        // when
        coupon.increaseIssuedQuantity();

        // then
        assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("발급 불가능한 쿠폰의 수량을 증가시키려고 하면 예외가 발생한다")
    void increaseIssuedQuantity_NotIssuable() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = new Coupon(
                "만료된 쿠폰",
                "PERCENTAGE",
                new DiscountRate(10),
                null,
                new Money(50000L),
                100,
                now.minusDays(10),
                now.minusDays(1)
        );

        // when & then
        assertThatThrownBy(() -> coupon.increaseIssuedQuantity())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("쿠폰을 발급할 수 없습니다.");
    }

    @Test
    @DisplayName("발급 수량을 감소시킨다")
    void decreaseIssuedQuantity_Success() {
        // given
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
        coupon.increaseIssuedQuantity();
        coupon.increaseIssuedQuantity();

        // when
        coupon.decreaseIssuedQuantity();

        // then
        assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("발급된 쿠폰이 없을 때 수량을 감소시키려고 하면 예외가 발생한다")
    void decreaseIssuedQuantity_NoIssuedCoupon() {
        // given
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

        // when & then
        assertThatThrownBy(() -> coupon.decreaseIssuedQuantity())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("발급된 쿠폰이 없습니다.");
    }
}
