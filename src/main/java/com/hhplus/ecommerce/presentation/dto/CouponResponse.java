package com.hhplus.ecommerce.presentation.dto;

import com.hhplus.ecommerce.domain.entity.Coupon;

import java.time.LocalDateTime;

public record CouponResponse(
        Long id,
        String name,
        String couponType,
        Integer discountPercentage,
        Integer discountAmount,
        Integer minOrderAmount,
        Integer totalQuantity,
        Integer issuedQuantity,
        Integer remainingQuantity,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean isIssuable
) {
    public static CouponResponse from(Coupon coupon) {
        Integer discountPercentage = coupon.getDiscountRate() != null
                ? coupon.getDiscountRate().getPercentage()
                : null;
        Integer discountAmount = coupon.getDiscountAmount() != null
                ? coupon.getDiscountAmount().getAmount().intValue()
                : null;
        Integer minOrderAmount = coupon.getMinOrderAmount() != null
                ? coupon.getMinOrderAmount().getAmount().intValue()
                : null;

        return new CouponResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getCouponType(),
                discountPercentage,
                discountAmount,
                minOrderAmount,
                coupon.getTotalQuantity(),
                coupon.getIssuedQuantity(),
                coupon.getTotalQuantity() - coupon.getIssuedQuantity(),
                coupon.getStartDate(),
                coupon.getEndDate(),
                coupon.isIssuable()
        );
    }
}
