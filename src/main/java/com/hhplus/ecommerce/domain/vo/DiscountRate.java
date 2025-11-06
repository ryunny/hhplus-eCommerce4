package com.hhplus.ecommerce.domain.vo;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class DiscountRate implements Serializable {

    private Integer percentage;

    public DiscountRate(Integer percentage) {
        validatePercentage(percentage);
        this.percentage = percentage;
    }

    private void validatePercentage(Integer percentage) {
        if (percentage == null) {
            throw new IllegalArgumentException("할인율은 null일 수 없습니다.");
        }
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("할인율은 0 이상 100 이하여야 합니다.");
        }
    }

    public Money applyTo(Money amount) {
        long discountedAmount = amount.getAmount() * (100 - percentage) / 100;
        return new Money(discountedAmount);
    }

    public Money calculateDiscountAmount(Money amount) {
        long discountAmount = amount.getAmount() * percentage / 100;
        return new Money(discountAmount);
    }

    @Override
    public String toString() {
        return percentage + "%";
    }
}
