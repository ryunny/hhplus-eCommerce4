package com.hhplus.ecommerce.domain.entity;

import com.hhplus.ecommerce.domain.enums.OrderStatus;
import com.hhplus.ecommerce.domain.vo.Money;
import com.hhplus.ecommerce.domain.vo.Phone;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    private Long id;
    private User user;
    private UserCoupon userCoupon;
    private String recipientName;
    private String shippingAddress;
    private Phone shippingPhone;
    private Money totalAmount;
    private Money discountAmount;
    private Money finalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public Order(User user, UserCoupon userCoupon, String recipientName,
                 String shippingAddress, Phone shippingPhone, Money totalAmount,
                 Money discountAmount, Money finalAmount, OrderStatus status) {
        this.user = user;
        this.userCoupon = userCoupon;
        this.recipientName = recipientName;
        this.shippingAddress = shippingAddress;
        this.shippingPhone = shippingPhone;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount != null ? discountAmount : Money.zero();
        this.finalAmount = finalAmount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }
}
