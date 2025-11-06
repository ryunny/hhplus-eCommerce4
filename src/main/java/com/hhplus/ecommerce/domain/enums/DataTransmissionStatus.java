package com.hhplus.ecommerce.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DataTransmissionStatus {
    PENDING("전송 대기"),
    SUCCESS("전송 성공"),
    FAILED("전송 실패");

    private final String description;
}
