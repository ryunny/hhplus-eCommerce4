package com.hhplus.ecommerce.application.usecase.cart;

import com.hhplus.ecommerce.domain.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 장바구니에서 상품 제거 UseCase
 *
 * User Story: "사용자가 장바구니에서 상품을 제거한다"
 */
@Service
public class RemoveFromCartUseCase {

    private final CartService cartService;

    public RemoveFromCartUseCase(CartService cartService) {
        this.cartService = cartService;
    }

    @Transactional
    public void execute(Long cartItemId) {
        cartService.removeFromCart(cartItemId);
    }
}
