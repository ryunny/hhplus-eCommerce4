package com.hhplus.ecommerce.domain.entity;

import com.hhplus.ecommerce.domain.enums.DataTransmissionStatus;
import com.hhplus.ecommerce.domain.enums.PaymentStatus;
import com.hhplus.ecommerce.domain.vo.Money;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    private Long id;
    private Order order;
    private Money paidAmount;
    private PaymentStatus status;
    private DataTransmissionStatus dataTransmissionStatus;
    private LocalDateTime createdAt;

    public Payment(Order order, Money paidAmount, PaymentStatus status,
                   DataTransmissionStatus dataTransmissionStatus) {
        this.order = order;
        this.paidAmount = paidAmount;
        this.status = status;
        this.dataTransmissionStatus = dataTransmissionStatus;
        this.createdAt = LocalDateTime.now();
    }

    public void updateStatus(PaymentStatus newStatus) {
        this.status = newStatus;
    }

    public void updateDataTransmissionStatus(DataTransmissionStatus newStatus) {
        this.dataTransmissionStatus = newStatus;
    }
}
