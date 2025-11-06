package com.hhplus.ecommerce.domain.enums;

public enum CouponQueueStatus {
    WAITING,    // 대기 중
    PROCESSING, // 처리 중
    COMPLETED,  // 발급 완료
    FAILED      // 발급 실패
}
