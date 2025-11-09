package com.hhplus.ecommerce.application.usecase;

import com.hhplus.ecommerce.domain.entity.Category;
import com.hhplus.ecommerce.domain.entity.Product;
import com.hhplus.ecommerce.domain.entity.User;
import com.hhplus.ecommerce.domain.vo.Email;
import com.hhplus.ecommerce.domain.vo.Money;
import com.hhplus.ecommerce.domain.vo.Phone;
import com.hhplus.ecommerce.domain.vo.Stock;
import com.hhplus.ecommerce.presentation.dto.CreateOrderRequest;
import com.hhplus.ecommerce.test.mocks.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class OrderConcurrencyTest {

    @Test
    @DisplayName("100명이 재고 10개인 상품을 동시에 구매할 때 정확히 10개만 판매된다")
    void createOrder_Concurrency_LimitedStock() throws InterruptedException {
        // given
        MockOrderRepository orderRepository = new MockOrderRepository();
        MockOrderItemRepository orderItemRepository = new MockOrderItemRepository();
        MockPaymentRepository paymentRepository = new MockPaymentRepository();
        MockUserRepository userRepository = new MockUserRepository();
        MockProductRepository productRepository = new MockProductRepository();
        MockUserCouponRepository userCouponRepository = new MockUserCouponRepository();

        // Service 생성
        com.hhplus.ecommerce.domain.service.OrderService orderService =
                new com.hhplus.ecommerce.domain.service.OrderService(orderRepository, orderItemRepository);
        com.hhplus.ecommerce.domain.service.ProductService productService =
                new com.hhplus.ecommerce.domain.service.ProductService(productRepository, orderItemRepository);
        com.hhplus.ecommerce.domain.service.UserService userService =
                new com.hhplus.ecommerce.domain.service.UserService(userRepository);
        com.hhplus.ecommerce.domain.service.CouponService couponService =
                new com.hhplus.ecommerce.domain.service.CouponService(userCouponRepository);
        com.hhplus.ecommerce.domain.service.PaymentService paymentService =
                new com.hhplus.ecommerce.domain.service.PaymentService(paymentRepository);

        OrderUseCase orderUseCase = new OrderUseCase(
                orderService,
                productService,
                userService,
                couponService,
                paymentService
        );

        // 재고 10개인 상품 추가
        Category category = new Category("테스트");
        Product limitedProduct = new Product(category, "한정 상품", "재고 10개", new Money(10000L), new Stock(10));
        Product savedProduct = productRepository.save(limitedProduct);

        // 100명의 사용자 생성 (충분한 잔액)
        for (int i = 1; i <= 100; i++) {
            User user = new User("User" + i, new Email("user" + i + "@test.com"), new Phone("010-0000-" + String.format("%04d", i)));
            user.chargeBalance(new Money(100000L));
            userRepository.save(user);
        }

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 100명이 동시에 구매 시도
        for (int i = 1; i <= threadCount; i++) {
            final long userId = i + 3L; // MockUserRepository 초기 데이터 3개 이후
            executorService.submit(() -> {
                try {
                    CreateOrderRequest request = new CreateOrderRequest(
                            List.of(new CreateOrderRequest.OrderItemRequest(savedProduct.getId(), 1)),
                            null,
                            "수령인",
                            "서울시 강남구",
                            "010-1234-5678"
                    );
                    orderUseCase.createOrderAndPay(userId, request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(10); // 정확히 10명만 성공
        assertThat(failCount.get()).isEqualTo(90); // 나머지 90명은 실패

        // 재고 확인
        Product updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getStock().getQuantity()).isEqualTo(0); // 재고 모두 소진
    }

    @Test
    @DisplayName("동일한 사용자가 여러 스레드에서 동시에 주문할 때 잔액이 정확히 차감된다")
    void createOrder_Concurrency_SameUser_BalanceDeduction() throws InterruptedException {
        // given
        MockOrderRepository orderRepository = new MockOrderRepository();
        MockOrderItemRepository orderItemRepository = new MockOrderItemRepository();
        MockPaymentRepository paymentRepository = new MockPaymentRepository();
        MockUserRepository userRepository = new MockUserRepository();
        MockProductRepository productRepository = new MockProductRepository();
        MockUserCouponRepository userCouponRepository = new MockUserCouponRepository();

        // Service 생성
        com.hhplus.ecommerce.domain.service.OrderService orderService =
                new com.hhplus.ecommerce.domain.service.OrderService(orderRepository, orderItemRepository);
        com.hhplus.ecommerce.domain.service.ProductService productService =
                new com.hhplus.ecommerce.domain.service.ProductService(productRepository, orderItemRepository);
        com.hhplus.ecommerce.domain.service.UserService userService =
                new com.hhplus.ecommerce.domain.service.UserService(userRepository);
        com.hhplus.ecommerce.domain.service.CouponService couponService =
                new com.hhplus.ecommerce.domain.service.CouponService(userCouponRepository);
        com.hhplus.ecommerce.domain.service.PaymentService paymentService =
                new com.hhplus.ecommerce.domain.service.PaymentService(paymentRepository);

        OrderUseCase orderUseCase = new OrderUseCase(
                orderService,
                productService,
                userService,
                couponService,
                paymentService
        );

        // 충분한 재고의 상품 생성
        Product product = productRepository.findById(1L).orElseThrow(); // 노트북 (890,000원, 재고 10개)

        // 사용자 생성 (잔액 1,000,000원, 1개 상품만 살 수 있음)
        User user = new User("TestUser", new Email("test@test.com"), new Phone("010-1111-1111"));
        user.chargeBalance(new Money(1000000L));
        User savedUser = userRepository.save(user);

        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 동일한 사용자가 5번 동시에 주문 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    CreateOrderRequest request = new CreateOrderRequest(
                            List.of(new CreateOrderRequest.OrderItemRequest(product.getId(), 1)),
                            null,
                            "수령인",
                            "서울시 강남구",
                            "010-1234-5678"
                    );
                    orderUseCase.createOrderAndPay(savedUser.getId(), request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1); // 1번만 성공 (잔액 부족으로)
        assertThat(failCount.get()).isEqualTo(4); // 나머지 4번은 실패

        // 잔액 확인
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getBalance().getAmount()).isEqualTo(1000000L - 890000L); // 1개 구매 금액만 차감
    }

    @Test
    @DisplayName("여러 사용자가 서로 다른 상품을 동시에 구매할 때 모두 성공한다")
    void createOrder_Concurrency_DifferentProducts() throws InterruptedException {
        // given
        MockOrderRepository orderRepository = new MockOrderRepository();
        MockOrderItemRepository orderItemRepository = new MockOrderItemRepository();
        MockPaymentRepository paymentRepository = new MockPaymentRepository();
        MockUserRepository userRepository = new MockUserRepository();
        MockProductRepository productRepository = new MockProductRepository();
        MockUserCouponRepository userCouponRepository = new MockUserCouponRepository();

        // Service 생성
        com.hhplus.ecommerce.domain.service.OrderService orderService =
                new com.hhplus.ecommerce.domain.service.OrderService(orderRepository, orderItemRepository);
        com.hhplus.ecommerce.domain.service.ProductService productService =
                new com.hhplus.ecommerce.domain.service.ProductService(productRepository, orderItemRepository);
        com.hhplus.ecommerce.domain.service.UserService userService =
                new com.hhplus.ecommerce.domain.service.UserService(userRepository);
        com.hhplus.ecommerce.domain.service.CouponService couponService =
                new com.hhplus.ecommerce.domain.service.CouponService(userCouponRepository);
        com.hhplus.ecommerce.domain.service.PaymentService paymentService =
                new com.hhplus.ecommerce.domain.service.PaymentService(paymentRepository);

        OrderUseCase orderUseCase = new OrderUseCase(
                orderService,
                productService,
                userService,
                couponService,
                paymentService
        );

        // 상품 3개 (MockProductRepository 초기 데이터)
        Long product1Id = 1L; // 노트북
        Long product2Id = 2L; // 마우스
        Long product3Id = 3L; // 키보드

        // 사용자 3명 (MockUserRepository 초기 데이터, 충분한 잔액 설정)
        User user1 = userRepository.findById(1L).orElseThrow();
        User user2 = userRepository.findById(2L).orElseThrow();
        User user3 = userRepository.findById(3L).orElseThrow();

        user1.chargeBalance(new Money(900000L));
        user2.chargeBalance(new Money(900000L));
        user3.chargeBalance(new Money(900000L));

        int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        // when - 각 사용자가 서로 다른 상품을 동시에 구매
        executorService.submit(() -> {
            try {
                CreateOrderRequest request = new CreateOrderRequest(
                        List.of(new CreateOrderRequest.OrderItemRequest(product1Id, 1)),
                        null,
                        "수령인1",
                        "서울시 강남구",
                        "010-1111-1111"
                );
                orderUseCase.createOrderAndPay(user1.getId(), request);
                successCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                CreateOrderRequest request = new CreateOrderRequest(
                        List.of(new CreateOrderRequest.OrderItemRequest(product2Id, 1)),
                        null,
                        "수령인2",
                        "서울시 강남구",
                        "010-2222-2222"
                );
                orderUseCase.createOrderAndPay(user2.getId(), request);
                successCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                CreateOrderRequest request = new CreateOrderRequest(
                        List.of(new CreateOrderRequest.OrderItemRequest(product3Id, 1)),
                        null,
                        "수령인3",
                        "서울시 강남구",
                        "010-3333-3333"
                );
                orderUseCase.createOrderAndPay(user3.getId(), request);
                successCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(3); // 모두 성공

        // 각 상품의 재고 확인
        Product updatedProduct1 = productRepository.findById(product1Id).orElseThrow();
        Product updatedProduct2 = productRepository.findById(product2Id).orElseThrow();
        Product updatedProduct3 = productRepository.findById(product3Id).orElseThrow();

        assertThat(updatedProduct1.getStock().getQuantity()).isEqualTo(9); // 10 - 1
        assertThat(updatedProduct2.getStock().getQuantity()).isEqualTo(49); // 50 - 1
        assertThat(updatedProduct3.getStock().getQuantity()).isEqualTo(29); // 30 - 1
    }
}
