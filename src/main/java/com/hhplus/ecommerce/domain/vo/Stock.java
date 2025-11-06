package com.hhplus.ecommerce.domain.vo;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Stock implements Serializable {

    private Integer quantity;

    public Stock(Integer quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("재고는 null일 수 없습니다.");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");
        }
    }

    public Stock decrease(Quantity decreaseQuantity) {
        int newQuantity = this.quantity - decreaseQuantity.getValue();
        if (newQuantity < 0) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        return new Stock(newQuantity);
    }

    public Stock increase(Quantity increaseQuantity) {
        return new Stock(this.quantity + increaseQuantity.getValue());
    }

    public boolean isSufficientFor(Quantity requiredQuantity) {
        return this.quantity >= requiredQuantity.getValue();
    }

    @Override
    public String toString() {
        return quantity + "개";
    }
}
