package com.hhplus.ecommerce.application.usecase.coupon;

import com.hhplus.ecommerce.domain.service.CouponService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 쿠폰 대기 순번 업데이트 UseCase (스케줄러)
 *
 * Background Job: "5초마다 대기 순번을 업데이트한다"
 */
@Service
public class UpdateQueuePositionsUseCase {

    private final CouponService couponService;

    public UpdateQueuePositionsUseCase(CouponService couponService) {
        this.couponService = couponService;
    }

    /**
     * 스케줄러로 실행되는 대기 순번 업데이트
     */
    @Scheduled(fixedDelay = 5000)
    public void execute() {
        couponService.updateQueuePositions();
    }
}
