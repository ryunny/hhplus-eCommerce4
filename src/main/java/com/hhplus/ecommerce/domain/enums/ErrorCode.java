package com.hhplus.ecommerce.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // User
    USER_NOT_FOUND("USR001", "사용자를 찾을 수 없습니다"),
    INSUFFICIENT_BALANCE("USR002", "잔액이 부족합니다"),

    // Product
    PRODUCT_NOT_FOUND("PRD001", "상품을 찾을 수 없습니다"),
    INSUFFICIENT_STOCK("PRD002", "재고가 부족합니다"),

    // Cart
    CART_ITEM_NOT_FOUND("CRT001", "장바구니 아이템을 찾을 수 없습니다"),

    // Order
    ORDER_NOT_FOUND("ORD001", "주문을 찾을 수 없습니다"),
    INVALID_ORDER_STATUS("ORD002", "유효하지 않은 주문 상태입니다"),

    // Coupon
    COUPON_NOT_FOUND("CPN001", "쿠폰을 찾을 수 없습니다"),
    COUPON_ALREADY_ISSUED("CPN002", "이미 발급된 쿠폰입니다"),
    COUPON_NOT_AVAILABLE("CPN003", "발급 가능한 쿠폰이 아닙니다"),
    COUPON_ALREADY_USED("CPN004", "이미 사용된 쿠폰입니다"),
    COUPON_EXPIRED("CPN005", "만료된 쿠폰입니다"),
    COUPON_UNAUTHORIZED("CPN006", "다른 사용자의 쿠폰입니다"),
    MIN_ORDER_AMOUNT_NOT_MET("CPN007", "최소 주문 금액을 충족하지 못했습니다"),

    // Category
    CATEGORY_NOT_FOUND("CTG001", "카테고리를 찾을 수 없습니다"),

    // Payment
    PAYMENT_FAILED("PAY001", "결제에 실패했습니다");

    private final String code;
    private final String message;
}
