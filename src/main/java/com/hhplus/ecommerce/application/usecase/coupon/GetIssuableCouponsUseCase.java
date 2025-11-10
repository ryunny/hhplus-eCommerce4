package com.hhplus.ecommerce.application.usecase.coupon;

import com.hhplus.ecommerce.domain.entity.Coupon;
import com.hhplus.ecommerce.domain.service.CouponService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 발급 가능한 쿠폰 목록 조회 UseCase
 *
 * User Story: "사용자가 발급 가능한 쿠폰 목록을 조회한다"
 */
@Service
public class GetIssuableCouponsUseCase {

    private final CouponService couponService;

    public GetIssuableCouponsUseCase(CouponService couponService) {
        this.couponService = couponService;
    }

    public List<Coupon> execute() {
        return couponService.getIssuableCoupons();
    }
}
