package com.hhplus.ecommerce.application.command;

/**
 * 장바구니 추가 Command
 */
public record AddToCartCommand(
        Long userId,
        Long productId,
        Integer quantity
) {
}
