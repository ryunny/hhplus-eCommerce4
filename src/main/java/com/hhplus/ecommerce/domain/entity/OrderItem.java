package com.hhplus.ecommerce.domain.entity;

import com.hhplus.ecommerce.domain.vo.Money;
import com.hhplus.ecommerce.domain.vo.Quantity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    private Long id;
    private Order order;
    private Product product;
    private Quantity quantity;
    private Money unitPrice;
    private Money subtotal;
    private LocalDateTime createdAt;

    public OrderItem(Order order, Product product, Quantity quantity,
                     Money unitPrice, Money subtotal) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.createdAt = LocalDateTime.now();
    }

    public static OrderItem create(Order order, Product product, Quantity quantity) {
        Money unitPrice = product.getPrice();
        Money subtotal = quantity.multiply(unitPrice);
        return new OrderItem(order, product, quantity, unitPrice, subtotal);
    }
}
