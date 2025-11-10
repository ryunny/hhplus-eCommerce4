package com.hhplus.ecommerce.presentation.controller;

import com.hhplus.ecommerce.application.command.IssueCouponCommand;
import com.hhplus.ecommerce.application.command.JoinCouponQueueCommand;
import com.hhplus.ecommerce.application.query.GetAvailableCouponsQuery;
import com.hhplus.ecommerce.application.query.GetQueueStatusQuery;
import com.hhplus.ecommerce.application.query.GetUserCouponsQuery;
import com.hhplus.ecommerce.application.usecase.coupon.*;
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

    private final GetIssuableCouponsUseCase getIssuableCouponsUseCase;
    private final IssueCouponUseCase issueCouponUseCase;
    private final GetUserCouponsUseCase getUserCouponsUseCase;
    private final GetAvailableCouponsUseCase getAvailableCouponsUseCase;
    private final JoinCouponQueueUseCase joinCouponQueueUseCase;
    private final GetQueueStatusUseCase getQueueStatusUseCase;

    @GetMapping("/issuable")
    public ResponseEntity<List<CouponResponse>> getIssuableCoupons() {
        List<Coupon> coupons = getIssuableCouponsUseCase.execute();
        List<CouponResponse> response = coupons.stream()
                .map(CouponResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{couponId}/issue/{userId}")
    public ResponseEntity<?> issueCoupon(
            @PathVariable Long couponId,
            @PathVariable Long userId) {
        IssueCouponCommand command = new IssueCouponCommand(userId, couponId);
        UserCoupon userCoupon = issueCouponUseCase.execute(command);

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
        GetUserCouponsQuery query = new GetUserCouponsQuery(userId);
        List<UserCoupon> userCoupons = getUserCouponsUseCase.execute(query);
        List<UserCouponResponse> response = userCoupons.stream()
                .map(UserCouponResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/available")
    public ResponseEntity<List<UserCouponResponse>> getAvailableCoupons(@PathVariable Long userId) {
        GetAvailableCouponsQuery query = new GetAvailableCouponsQuery(userId);
        List<UserCoupon> userCoupons = getAvailableCouponsUseCase.execute(query);
        List<UserCouponResponse> response = userCoupons.stream()
                .map(UserCouponResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    // ===== 대기열 API =====

    /**
     * 대기열 진입 (선착순 쿠폰)
     */
    @PostMapping("/{couponId}/queue/join/{userId}")
    public ResponseEntity<CouponQueueResponse> joinQueue(
            @PathVariable Long couponId,
            @PathVariable Long userId) {
        JoinCouponQueueCommand command = new JoinCouponQueueCommand(userId, couponId);
        CouponQueue queue = joinCouponQueueUseCase.execute(command);
        return ResponseEntity.ok(CouponQueueResponse.from(queue));
    }

    /**
     * 대기 상태 조회
     */
    @GetMapping("/{couponId}/queue/status/{userId}")
    public ResponseEntity<CouponQueueResponse> getQueueStatus(
            @PathVariable Long couponId,
            @PathVariable Long userId) {
        GetQueueStatusQuery query = new GetQueueStatusQuery(userId, couponId);
        CouponQueue queue = getQueueStatusUseCase.execute(query);
        return ResponseEntity.ok(CouponQueueResponse.from(queue));
    }
}
