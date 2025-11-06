package com.hhplus.ecommerce.presentation.dto;

import com.hhplus.ecommerce.domain.entity.UserCoupon;

import java.time.LocalDateTime;

public record UserCouponResponse(
        Long id,
        Long userId,
        Long couponId,
        String couponName,
        String status,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt
) {
    public static UserCouponResponse from(UserCoupon userCoupon) {
        return new UserCouponResponse(
                userCoupon.getId(),
                userCoupon.getUser().getId(),
                userCoupon.getCoupon().getId(),
                userCoupon.getCoupon().getName(),
                userCoupon.getStatus().name(),
                userCoupon.getIssuedAt(),
                userCoupon.getExpiresAt()
        );
    }
}
