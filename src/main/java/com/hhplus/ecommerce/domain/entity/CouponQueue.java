package com.hhplus.ecommerce.domain.entity;

import com.hhplus.ecommerce.domain.enums.CouponQueueStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponQueue {

    private Long id;
    private User user;
    private Coupon coupon;
    private CouponQueueStatus status;
    private Integer queuePosition;
    private LocalDateTime processedAt;
    private String failedReason;
    private LocalDateTime createdAt;

    public CouponQueue(User user, Coupon coupon, CouponQueueStatus status, Integer queuePosition) {
        this.user = user;
        this.coupon = coupon;
        this.status = status;
        this.queuePosition = queuePosition;
        this.createdAt = LocalDateTime.now();
    }

    public void updateStatus(CouponQueueStatus newStatus) {
        this.status = newStatus;
        if (newStatus == CouponQueueStatus.COMPLETED || newStatus == CouponQueueStatus.FAILED) {
            this.processedAt = LocalDateTime.now();
        }
    }

    public void setFailedReason(String reason) {
        this.failedReason = reason;
        this.status = CouponQueueStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }

    public void updateQueuePosition(Integer newPosition) {
        this.queuePosition = newPosition;
    }
}
