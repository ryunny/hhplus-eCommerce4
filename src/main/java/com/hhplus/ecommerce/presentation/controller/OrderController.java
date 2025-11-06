package com.hhplus.ecommerce.presentation.controller;

import com.hhplus.ecommerce.application.usecase.OrderUseCase;
import com.hhplus.ecommerce.domain.entity.Order;
import com.hhplus.ecommerce.presentation.dto.CreateOrderRequest;
import com.hhplus.ecommerce.presentation.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCase orderUseCase;

    @PostMapping("/{userId}")
    public ResponseEntity<OrderResponse> createOrder(
            @PathVariable Long userId,
            @RequestBody CreateOrderRequest request) {
        Order order = orderUseCase.createOrderAndPay(userId, request);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        Order order = orderUseCase.getOrder(orderId);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderUseCase.getUserOrders(userId);
        List<OrderResponse> response = orders.stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
