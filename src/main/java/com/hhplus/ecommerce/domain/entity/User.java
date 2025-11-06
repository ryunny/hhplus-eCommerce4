package com.hhplus.ecommerce.domain.entity;

import com.hhplus.ecommerce.domain.vo.Email;
import com.hhplus.ecommerce.domain.vo.Money;
import com.hhplus.ecommerce.domain.vo.Phone;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    private Long id;
    private String name;
    private Email email;
    private Phone phone;
    private Money balance;
    private LocalDateTime createdAt;

    public User(String name, Email email, Phone phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.balance = Money.zero();
        this.createdAt = LocalDateTime.now();
    }

    public void chargeBalance(Money amount) {
        if (amount.getAmount() <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        this.balance = this.balance.add(amount);
    }

    public void deductBalance(Money amount) {
        if (amount.getAmount() <= 0) {
            throw new IllegalArgumentException("차감 금액은 0보다 커야 합니다.");
        }
        if (this.balance.isLessThan(amount)) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
        this.balance = this.balance.subtract(amount);
    }

    public boolean hasEnoughBalance(Money required) {
        return this.balance.isGreaterThanOrEqual(required);
    }
}
