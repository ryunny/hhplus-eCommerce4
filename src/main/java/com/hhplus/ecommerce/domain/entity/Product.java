package com.hhplus.ecommerce.domain.entity;

import com.hhplus.ecommerce.domain.vo.Money;
import com.hhplus.ecommerce.domain.vo.Quantity;
import com.hhplus.ecommerce.domain.vo.Stock;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    private Long id;
    private Category category;
    private String name;
    private String description;
    private Money price;
    private Stock stock;
    private LocalDateTime createdAt;

    public Product(Category category, String name, String description, Money price, Stock stock) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.createdAt = LocalDateTime.now();
    }

    public void decreaseStock(Quantity quantity) {
        if (quantity.getValue() <= 0) {
            throw new IllegalArgumentException("차감할 수량은 0보다 커야 합니다.");
        }
        this.stock = this.stock.decrease(quantity);
    }

    public void increaseStock(Quantity quantity) {
        if (quantity.getValue() <= 0) {
            throw new IllegalArgumentException("추가할 수량은 0보다 커야 합니다.");
        }
        this.stock = this.stock.increase(quantity);
    }

    public boolean hasSufficientStock(Quantity requiredQuantity) {
        return this.stock.isSufficientFor(requiredQuantity);
    }
}
