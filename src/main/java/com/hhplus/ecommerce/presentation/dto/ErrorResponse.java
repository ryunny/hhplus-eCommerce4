package com.hhplus.ecommerce.presentation.dto;

import com.hhplus.ecommerce.domain.enums.ErrorCode;

import java.time.LocalDateTime;

public record ErrorResponse(
        String errorCode,
        String message,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                LocalDateTime.now()
        );
    }

    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(
                errorCode,
                message,
                LocalDateTime.now()
        );
    }
}
