package com.hhplus.ecommerce.presentation.controller;

import com.hhplus.ecommerce.application.usecase.CouponUseCase;
import com.hhplus.ecommerce.domain.entity.Coupon;
import com.hhplus.ecommerce.domain.entity.CouponQueue;
import com.hhplus.ecommerce.domain.entity.UserCoupon;
import com.hhplus.ecommerce.presentation.dto.CouponQueueResponse;
import com.hhplus.ecommerce.presentation.dto.CouponResponse;
import com.hhplus.ecommerce.presentation.dto.MessageResponse;
import com.hhplus.ecommerce.presentation.dto.UserCouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponUseCase couponUseCase;

    @GetMapping("/issuable")
    public ResponseEntity<List<CouponResponse>> getIssuableCoupons() {
        List<Coupon> coupons = couponUseCase.getIssuableCoupons();
        List<CouponResponse> response = coupons.stream()
                .map(CouponResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{couponId}/issue/{userId}")
    public ResponseEntity<?> issueCoupon(
            @PathVariable Long couponId,
            @PathVariable Long userId) {
        UserCoupon userCoupon = couponUseCase.issueCoupon(userId, couponId);

        if (userCoupon == null) {
            // 대기열 방식: 대기열에 추가됨
            return ResponseEntity.accepted()
                    .body(new MessageResponse("대기열에 추가되었습니다. 상태를 확인해주세요."));
        }

        // 즉시 발급 방식
        return ResponseEntity.ok(UserCouponResponse.from(userCoupon));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserCouponResponse>> getUserCoupons(@PathVariable Long userId) {
        List<UserCoupon> userCoupons = couponUseCase.getUserCoupons(userId);
        List<UserCouponResponse> response = userCoupons.stream()
                .map(UserCouponResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/available")
    public ResponseEntity<List<UserCouponResponse>> getAvailableCoupons(@PathVariable Long userId) {
        List<UserCoupon> userCoupons = couponUseCase.getAvailableCoupons(userId);
        List<UserCouponResponse> response = userCoupons.stream()
                .map(UserCouponResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/expire")
    public ResponseEntity<Void> expireOldCoupons() {
        couponUseCase.expireOldCoupons();
        return ResponseEntity.ok().build();
    }

    // ===== 대기열 API =====

    /**
     * 대기열 진입 (선착순 쿠폰)
     */
    @PostMapping("/{couponId}/queue/join/{userId}")
    public ResponseEntity<CouponQueueResponse> joinQueue(
            @PathVariable Long couponId,
            @PathVariable Long userId) {
        CouponQueue queue = couponUseCase.joinQueue(userId, couponId);
        return ResponseEntity.ok(CouponQueueResponse.from(queue));
    }

    /**
     * 대기 상태 조회
     */
    @GetMapping("/{couponId}/queue/status/{userId}")
    public ResponseEntity<CouponQueueResponse> getQueueStatus(
            @PathVariable Long couponId,
            @PathVariable Long userId) {
        CouponQueue queue = couponUseCase.getQueueStatus(userId, couponId);
        return ResponseEntity.ok(CouponQueueResponse.from(queue));
    }
}
