package com.hhplus.ecommerce.domain.entity;

import com.hhplus.ecommerce.domain.vo.Money;
import com.hhplus.ecommerce.domain.vo.Quantity;
import com.hhplus.ecommerce.domain.vo.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    @DisplayName("상품 재고를 성공적으로 차감한다")
    void decreaseStock_Success() {
        // given
        Category category = new Category("전자제품");
        Product product = new Product(category, "노트북", "고성능 노트북", new Money(1000000L), new Stock(10));

        // when
        product.decreaseStock(new Quantity(3));

        // then
        assertThat(product.getStock().getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("재고가 부족하면 예외가 발생한다")
    void decreaseStock_InsufficientStock() {
        // given
        Category category = new Category("전자제품");
        Product product = new Product(category, "노트북", "고성능 노트북", new Money(1000000L), new Stock(5));

        // when & then
        assertThatThrownBy(() -> product.decreaseStock(new Quantity(10)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("재고가 부족합니다.");
    }

    @Test
    @DisplayName("0 이하의 수량으로 재고를 차감하려고 하면 예외가 발생한다")
    void decreaseStock_InvalidQuantity() {
        // given
        Category category = new Category("전자제품");
        Product product = new Product(category, "노트북", "고성능 노트북", new Money(1000000L), new Stock(10));

        // when & then
        assertThatThrownBy(() -> product.decreaseStock(new Quantity(0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("차감할 수량은 0보다 커야 합니다.");

        assertThatThrownBy(() -> product.decreaseStock(new Quantity(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수량은 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("상품 재고를 성공적으로 증가시킨다")
    void increaseStock_Success() {
        // given
        Category category = new Category("전자제품");
        Product product = new Product(category, "노트북", "고성능 노트북", new Money(1000000L), new Stock(10));

        // when
        product.increaseStock(new Quantity(5));

        // then
        assertThat(product.getStock().getQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("0 이하의 수량으로 재고를 증가시키려고 하면 예외가 발생한다")
    void increaseStock_InvalidQuantity() {
        // given
        Category category = new Category("전자제품");
        Product product = new Product(category, "노트북", "고성능 노트북", new Money(1000000L), new Stock(10));

        // when & then
        assertThatThrownBy(() -> product.increaseStock(new Quantity(0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("추가할 수량은 0보다 커야 합니다.");

        assertThatThrownBy(() -> product.increaseStock(new Quantity(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수량은 0 이상이어야 합니다.");
    }
}
