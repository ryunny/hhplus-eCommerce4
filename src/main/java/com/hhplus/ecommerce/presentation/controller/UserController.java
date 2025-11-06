package com.hhplus.ecommerce.presentation.controller;

import com.hhplus.ecommerce.application.usecase.UserBalanceUseCase;
import com.hhplus.ecommerce.domain.entity.User;
import com.hhplus.ecommerce.presentation.dto.ChargeBalanceRequest;
import com.hhplus.ecommerce.presentation.dto.UserBalanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserBalanceUseCase userBalanceUseCase;

    @PostMapping("/{userId}/balance/charge")
    public ResponseEntity<UserBalanceResponse> chargeBalance(
            @PathVariable Long userId,
            @RequestBody ChargeBalanceRequest request) {
        User user = userBalanceUseCase.chargeBalance(userId, request.amount());
        return ResponseEntity.ok(new UserBalanceResponse(user.getId(), user.getBalance().getAmount()));
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<UserBalanceResponse> getBalance(@PathVariable Long userId) {
        User user = userBalanceUseCase.getBalance(userId);
        return ResponseEntity.ok(new UserBalanceResponse(user.getId(), user.getBalance().getAmount()));
    }
}
