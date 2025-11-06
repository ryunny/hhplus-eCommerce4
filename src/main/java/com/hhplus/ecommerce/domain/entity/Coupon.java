package com.hhplus.ecommerce.domain.entity;

import com.hhplus.ecommerce.domain.vo.DiscountRate;
import com.hhplus.ecommerce.domain.vo.Money;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    private Long id;
    private String name;
    private String couponType;
    private DiscountRate discountRate;
    private Money discountAmount;
    private Money minOrderAmount;
    private Integer totalQuantity;
    private Integer issuedQuantity = 0;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean useQueue = false; // 대기열 사용 여부 (기본값: 즉시 발급)

    public Coupon(String name, String couponType, DiscountRate discountRate,
                  Money discountAmount, Money minOrderAmount, Integer totalQuantity,
                  LocalDateTime startDate, LocalDateTime endDate) {
        validateTotalQuantity(totalQuantity);
        this.name = name;
        this.couponType = couponType;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.minOrderAmount = minOrderAmount;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = 0;
        this.startDate = startDate;
        this.endDate = endDate;
        this.useQueue = false;
    }

    public Coupon(String name, String couponType, DiscountRate discountRate,
                  Money discountAmount, Money minOrderAmount, Integer totalQuantity,
                  LocalDateTime startDate, LocalDateTime endDate, boolean useQueue) {
        validateTotalQuantity(totalQuantity);
        this.name = name;
        this.couponType = couponType;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.minOrderAmount = minOrderAmount;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = 0;
        this.startDate = startDate;
        this.endDate = endDate;
        this.useQueue = useQueue;
    }

    private void validateTotalQuantity(Integer totalQuantity) {
        if (totalQuantity == null || totalQuantity < 0) {
            throw new IllegalArgumentException("총 발급 수량은 0 이상이어야 합니다.");
        }
    }

    public boolean isIssuable() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate) && now.isBefore(endDate)
                && issuedQuantity < totalQuantity;
    }

    public void increaseIssuedQuantity() {
        if (!isIssuable()) {
            throw new IllegalStateException("쿠폰을 발급할 수 없습니다.");
        }
        this.issuedQuantity++;
    }

    public void decreaseIssuedQuantity() {
        if (this.issuedQuantity <= 0) {
            throw new IllegalStateException("발급된 쿠폰이 없습니다.");
        }
        this.issuedQuantity--;
    }

    public Money calculateDiscount(Money orderAmount) {
        if (minOrderAmount != null && orderAmount.isLessThan(minOrderAmount)) {
            return Money.zero();
        }

        if (discountRate != null) {
            return discountRate.calculateDiscountAmount(orderAmount);
        } else if (discountAmount != null) {
            return discountAmount;
        }

        return Money.zero();
    }
}
