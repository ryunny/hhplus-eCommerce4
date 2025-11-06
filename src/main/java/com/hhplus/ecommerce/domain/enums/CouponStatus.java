package com.hhplus.ecommerce.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponStatus {
    UNUSED("미사용"),
    USED("사용됨"),
    EXPIRED("만료됨");

    private final String description;
}
