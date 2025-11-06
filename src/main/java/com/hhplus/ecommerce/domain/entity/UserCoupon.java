package com.hhplus.ecommerce.domain.entity;

import com.hhplus.ecommerce.domain.enums.CouponStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon {

    private Long id;
    private User user;
    private Coupon coupon;
    private CouponStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    public UserCoupon(User user, Coupon coupon, CouponStatus status, LocalDateTime expiresAt) {
        this.user = user;
        this.coupon = coupon;
        this.status = status;
        this.expiresAt = expiresAt;
        this.issuedAt = LocalDateTime.now();
    }

    public void use() {
        if (this.status != CouponStatus.UNUSED) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }
        this.status = CouponStatus.USED;
    }

    public void cancel() {
        if (this.status != CouponStatus.USED) {
            throw new IllegalStateException("사용된 쿠폰만 취소할 수 있습니다.");
        }
        this.status = CouponStatus.UNUSED;
    }

    public void expire() {
        this.status = CouponStatus.EXPIRED;
    }
}
