package com.hhplus.ecommerce.presentation.controller;

import com.hhplus.ecommerce.application.query.GetOrderQuery;
import com.hhplus.ecommerce.application.query.GetUserOrdersQuery;
import com.hhplus.ecommerce.application.usecase.order.GetOrderUseCase;
import com.hhplus.ecommerce.application.usecase.order.GetUserOrdersUseCase;
import com.hhplus.ecommerce.application.usecase.order.PlaceOrderUseCase;
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

    private final PlaceOrderUseCase placeOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final GetUserOrdersUseCase getUserOrdersUseCase;

    @PostMapping("/{userId}")
    public ResponseEntity<OrderResponse> createOrder(
            @PathVariable Long userId,
            @RequestBody CreateOrderRequest request) {
        Order order = placeOrderUseCase.execute(userId, request);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        GetOrderQuery query = new GetOrderQuery(orderId);
        Order order = getOrderUseCase.execute(query);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@PathVariable Long userId) {
        GetUserOrdersQuery query = new GetUserOrdersQuery(userId);
        List<Order> orders = getUserOrdersUseCase.execute(query);
        List<OrderResponse> response = orders.stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
