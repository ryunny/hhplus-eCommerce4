package com.hhplus.ecommerce.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund {

    private Long id;
    private Order order;
    private Long refundAmount;
    private String reason;
    private String status;
    private LocalDateTime createdAt;

    public Refund(Order order, Long refundAmount, String reason, String status) {
        this.order = order;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
    }
}
