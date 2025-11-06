package com.hhplus.ecommerce.domain.entity;

import com.hhplus.ecommerce.domain.vo.Quantity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    private Long id;
    private User user;
    private Product product;
    private Quantity quantity;
    private LocalDateTime createdAt;

    public CartItem(User user, Product product, Quantity quantity) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.createdAt = LocalDateTime.now();
    }

    public void updateQuantity(Quantity newQuantity) {
        this.quantity = newQuantity;
    }

    public void increaseQuantity(Quantity amount) {
        this.quantity = this.quantity.add(amount);
    }
}
