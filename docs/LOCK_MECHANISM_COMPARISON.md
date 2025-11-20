# 락 메커니즘 비교 분석 (Pessimistic Lock vs Optimistic Lock)

## 목차
1. [개요](#개요)
2. [테스트 환경](#테스트-환경)
3. [비관적 락 (Pessimistic Lock)](#1-비관적-락-pessimistic-lock)
4. [낙관적 락 (Optimistic Lock)](#2-낙관적-락-optimistic-lock)
5. [통합 테스트 결과](#통합-테스트-결과)
6. [비교 분석](#비교-분석)
7. [결론 및 선택 이유](#결론-및-선택-이유)
8. [참고: 대안적 접근법](#참고-대안적-접근법)

---

## 개요

이커머스 프로젝트에서 **동시성 제어**는 핵심 요구사항입니다. 특히 재고 관리, 잔액 관리, 쿠폰 발급 등에서 여러 사용자가 동시에 같은 데이터를 수정할 때 **데이터 정합성**을 보장해야 합니다.

본 문서는 두 가지 **락 메커니즘**을 실제로 구현하고 통합 테스트를 통해 비교 분석한 결과를 정리합니다:
- **비관적 락** (Pessimistic Lock)
- **낙관적 락** (Optimistic Lock)

---

## 테스트 환경

### 테스트 시나리오
- **동시 요청 수**: 100개 스레드
- **초기 재고**: 10개
- **구매 수량**: 각 1개
- **예상 결과**: 정확히 10명만 성공, 90명 실패, 최종 재고 0

### 기술 스택
- **Spring Boot** 3.5.7
- **JPA/Hibernate** 6.6.33
- **MySQL** 8.0
- **Testcontainers** 1.19.3

---

## 1. 비관적 락 (Pessimistic Lock)

### 개념
**"충돌이 발생할 것이라고 비관적으로 가정"**하고, **선제적으로 데이터를 잠금**합니다.

### 구현 방식
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Optional<Product> findByIdWithLock(@Param("id") Long id);
```

**생성되는 SQL:**
```sql
SELECT * FROM products WHERE id = ? FOR UPDATE
```

### 동작 원리
1. **트랜잭션 시작**
2. `SELECT ... FOR UPDATE`로 **행 잠금 획득**
3. 다른 트랜잭션은 락이 해제될 때까지 **대기 (Blocking)**
4. 재고 감소 및 저장
5. **트랜잭션 커밋 후 락 해제**
6. 대기 중이던 다음 트랜잭션이 락 획득

### 장점
- ✅ **데이터 정합성 100% 보장**
- ✅ 충돌이 확실히 예상될 때 효율적
- ✅ 재시도 로직 불필요
- ✅ 구현 단순 (애플리케이션 레벨 로직 최소화)

### 단점
- ❌ **트랜잭션 대기 시간 발생** (병목 가능성)
- ❌ **데드락 위험** (여러 테이블 잠금 시)
- ❌ 동시 처리량 감소 (순차 처리)
- ❌ 락 대기로 인한 응답 시간 증가

### 데드락 예시
```java
// Thread 1: Product A → Product B 순서로 잠금
// Thread 2: Product B → Product A 순서로 잠금
// → 서로 상대방의 락을 기다리며 데드락 발생
```

---

## 2. 낙관적 락 (Optimistic Lock)

### 개념
**"충돌이 거의 없을 것이라고 낙관적으로 가정"**하고, **커밋 시점에만 검증**합니다.

### 구현 방식
```java
@Entity
public class Product {
    @Id
    private Long id;

    private Integer stock;

    @Version  // 낙관적 락
    private Long version;
}
```

**생성되는 SQL:**
```sql
-- 조회 시 (version 함께 읽기)
SELECT id, stock, version FROM products WHERE id = ?

-- 업데이트 시 (version 비교)
UPDATE products
SET stock = ?, version = version + 1
WHERE id = ? AND version = ?  -- 현재 version과 비교
```

### 동작 원리
1. **트랜잭션 시작** (락 없음)
2. 데이터 조회 시 `version` 함께 읽기
3. 비즈니스 로직 수행 (다른 트랜잭션과 병렬 실행)
4. **커밋 시점에 `version` 비교**
5. 만약 다른 트랜잭션이 먼저 수정했다면:
   - DB의 `version`이 증가되어 있음
   - `WHERE version = ?` 조건 불만족
   - **업데이트 실패 → `OptimisticLockException` 발생**
6. 애플리케이션에서 **재시도 처리 필요**

### 장점
- ✅ **충돌이 적을 때 성능 우수**
- ✅ 데드락 위험 없음
- ✅ 동시 읽기 성능 우수 (락 없음)
- ✅ 트랜잭션 대기 없음

### 단점
- ❌ **충돌이 많으면 대량 실패 발생**
- ❌ **재시도 로직 필수** (코드 복잡도 증가)
- ❌ 재시도 횟수가 많으면 오히려 성능 저하
- ❌ `version` 필드 관리 필요

### 재시도 로직 예시
```java
@Retryable(
    value = ObjectOptimisticLockingFailureException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 100)
)
public void decreaseStock(Long productId, int quantity) {
    Product product = productRepository.findById(productId)
        .orElseThrow();
    product.decreaseStock(quantity);
    productRepository.save(product);  // version 체크
}
```

---

## 통합 테스트 결과

### 테스트 코드 위치
```
src/test/java/com/hhplus/ecommerce/concurrency/LockMechanismComparisonTest.java
```

### 1. 비관적 락 테스트 결과

```
========== 비관적 락 테스트 결과 ==========
성공 횟수: 10
실패 횟수: 90
최종 재고: 0
전체 실행 시간: 약 1,100ms
평균 트랜잭션 시간: 약 100ms
=========================================
```

**분석:**
- ✅ **정확히 10명만 성공** (정합성 100%)
- ✅ 최종 재고 0 (데이터 정확)
- ⚠️ **직렬 처리로 인한 대기 시간 발생**
- ⚠️ 평균 트랜잭션 시간 100ms (락 대기 포함)

**실행 흐름:**
```
Thread 1: 락 획득 → 재고 감소 → 커밋 → 락 해제 (성공)
Thread 2: [대기...] → 락 획득 → 재고 감소 → 커밋 → 락 해제 (성공)
...
Thread 11: [대기...] → 락 획득 → 재고 부족 → 실패
...
Thread 100: [대기...] → 락 획득 → 재고 부족 → 실패
```

---

### 2. 낙관적 락 테스트 결과 (재시도 없음)

```
========== 낙관적 락 테스트 결과 (재시도 없음) ==========
성공 횟수: 3~5 (실행마다 상이)
낙관적 락 충돌 횟수: 95~97
재고 부족 실패 횟수: 0
최종 재고: 5~7 (10 - 성공 횟수)
전체 실행 시간: 약 1,700ms
평균 트랜잭션 시간: 약 300~500ms
=========================================
```

**분석:**
- ❌ **재시도 없이는 대부분 실패** (95% 이상 충돌)
- ❌ 최종 재고가 남음 (데이터 정합성 미달)
- ❌ 성공 횟수가 실행마다 달라짐 (비결정적)
- ⚠️ 재시도 로직 구현 시 복잡도 증가 및 성능 저하 예상

**실행 흐름:**
```
Thread 1: 조회 (version=0) → 재고 감소 → 커밋 (성공, version=1)
Thread 2: 조회 (version=0) → 재고 감소 → 커밋 (실패! version이 1로 변경됨)
Thread 3: 조회 (version=0) → 재고 감소 → 커밋 (실패! version이 1로 변경됨)
Thread 4: 조회 (version=1) → 재고 감소 → 커밋 (성공, version=2)
...
→ 대부분의 스레드가 충돌로 실패
```

**재시도 구현 시 예상:**
- 재시도 3회 시: 성공률 증가하나 전체 실행 시간 **2배 이상 증가** 예상
- 여전히 일부 실패 가능성 존재
- 코드 복잡도 증가

---

## 비교 분석

### 종합 비교표

| 항목 | 비관적 락 | 낙관적 락 (재시도 없음) |
|------|----------|----------------------|
| **정합성 보장** | ✅ 100% | ❌ 불완전 (재시도 필요) |
| **최종 재고** | 0 (정확) | 5~7 (부정확) |
| **성공 횟수** | 10 | 3~5 |
| **충돌 실패 횟수** | 0 | 95~97 |
| **전체 실행 시간** | ~1,100ms | ~1,700ms |
| **평균 트랜잭션 시간** | ~100ms | ~300-500ms |
| **동시 처리** | ❌ 순차 대기 | ✅ 병렬 (충돌 시 실패) |
| **데드락 위험** | ⚠️ 있음 | ✅ 없음 |
| **재시도 로직** | 불필요 | **필수** |
| **코드 복잡도** | 낮음 | 높음 (재시도 구현) |
| **적합한 시나리오** | 충돌 많음 | 충돌 적음 |

---

### 핵심 차이점

#### 1. 락의 시점
- **비관적 락**: 조회 시점에 잠금 (`SELECT ... FOR UPDATE`)
- **낙관적 락**: 커밋 시점에 검증 (`UPDATE ... WHERE version = ?`)

#### 2. 충돌 처리 방식
- **비관적 락**: 충돌 방지 (다른 트랜잭션 대기)
- **낙관적 락**: 충돌 감지 (예외 발생 → 재시도)

#### 3. 성능 특성
- **비관적 락**:
  - 충돌 많을 때: 효율적 (대기만 하면 됨)
  - 충돌 적을 때: 비효율적 (불필요한 락)

- **낙관적 락**:
  - 충돌 적을 때: 효율적 (락 오버헤드 없음)
  - 충돌 많을 때: 비효율적 (대량 재시도)

---

### 시나리오별 권장 사항

#### 비관적 락 사용 시나리오 ✅

**언제 사용?**
- ✅ **충돌이 확실히 예상**되는 경우
- ✅ 복잡한 비즈니스 로직 (여러 테이블 조인, 다중 업데이트)
- ✅ 데이터 정합성이 최우선
- ✅ 재시도 로직 구현이 어려운 경우

**현재 프로젝트 적용 사례:**
- ✅ **쿠폰 선착순 발급** (Coupon + UserCoupon 두 테이블 업데이트)
- ✅ **사용자 잔액 차감** (User 잔액 + Order + Payment 다중 트랜잭션)
- ✅ **재고 감소** (Product 재고 + OrderItem 생성)

**주의사항:**
- ⚠️ 락 획득 순서 일관성 유지 (데드락 방지)
- ⚠️ 트랜잭션 크기 최소화 (락 보유 시간 단축)
- ⚠️ 타임아웃 설정 (무한 대기 방지)

---

#### 낙관적 락 사용 시나리오 ✅

**언제 사용?**
- ✅ **충돌이 거의 없는** 경우
- ✅ 읽기가 많고 쓰기가 적은 경우
- ✅ 재시도 로직 구현 가능
- ✅ 응답 시간보다 동시성이 중요한 경우

**적용 사례:**
- ✅ **사용자 프로필 수정** (동일 사용자의 동시 수정은 거의 없음)
- ✅ **게시글 수정** (작성자만 수정 가능)
- ✅ **설정 변경** (충돌 가능성 낮음)

**부적합 사례:**
- ❌ 재고 감소 (경쟁이 치열)
- ❌ 쿠폰 발급 (선착순)
- ❌ 좌석 예약 (동시 신청 많음)

---

## 결론 및 선택 이유

### 현재 프로젝트 선택: **비관적 락**

#### 선택 근거

#### 1. 데이터 정합성 최우선 ⭐
```
이커머스 특성상:
- 재고 오버셀링 → 고객 불만, 환불 처리, 신뢰도 하락
- 잔액 이중 차감 → 금전적 손실, 법적 문제
- 쿠폰 중복 발급 → 사업 손실

→ 성능보다 정합성이 더 중요!
```

#### 2. 충돌 발생이 확실한 시나리오
```
테스트 결과:
- 100명이 재고 10개를 동시 구매
- 낙관적 락: 95% 이상 충돌 발생
- 재시도 3회 시 전체 시간 2배+ 증가 예상

→ 비관적 락이 오히려 효율적!
```

#### 3. 복잡한 비즈니스 로직
```java
// 쿠폰 발급 예시
@Transactional
public void issueCoupon(Long userId, Long couponId) {
    // 1. Coupon 테이블 업데이트 (재고 감소)
    Coupon coupon = couponRepository.findByIdWithLock(couponId);
    coupon.issue();

    // 2. UserCoupon 테이블 INSERT
    UserCoupon userCoupon = new UserCoupon(user, coupon);
    userCouponRepository.save(userCoupon);

    // 3. 중복 발급 체크
    // ...
}
```
→ 여러 테이블 연관 업데이트는 낙관적 락으로 처리 불가

#### 4. 재시도 로직 부담
```java
// 낙관적 락 재시도 구현 시
@Retryable(maxAttempts = 3)  // 복잡도 증가
+ @Transactional
+ 예외 핸들링
+ 재시도 간격 조정
+ 최대 재시도 횟수 관리
+ 로깅 및 모니터링

→ 코드 복잡도 대비 효과 미미
```

#### 5. 성능 vs 정합성 트레이드오프
```
비관적 락:
- 평균 트랜잭션 시간: ~100ms
- 사용자 체감: 거의 차이 없음 (100ms vs 50ms)
- 정합성: 100% 보장

낙관적 락:
- 평균 트랜잭션 시간: ~300-500ms (재시도 시 더 증가)
- 사용자 체감: 오히려 느려질 수 있음
- 정합성: 재시도 후에도 일부 실패 가능

→ 비관적 락이 모든 면에서 우수!
```

---

### 예외적 적용: 낙관적 락

충돌이 거의 없는 경우에만 낙관적 락 사용:

```java
// 사용자 프로필 수정 (동일 사용자의 동시 수정은 거의 없음)
@Entity
public class UserProfile {
    @Version
    private Long version;

    private String nickname;
    private String bio;
    // ...
}
```

---

## 참고: 대안적 접근법

### 락이 아닌 다른 동시성 제어 방법

락 외에도 동시성을 제어할 수 있는 방법들이 있습니다:

#### 1. 조건부 업데이트 (Conditional Update)

**개념:** `WHERE` 조건으로 원자적 업데이트

```java
@Modifying
@Query(value = "UPDATE products SET stock = stock - :quantity " +
       "WHERE id = :id AND stock >= :quantity", nativeQuery = true)
int decreaseStockConditionally(@Param("id") Long id, @Param("quantity") int quantity);
```

**장점:**
- ✅ 단일 쿼리로 원자적 처리
- ✅ 락 오버헤드 없음
- ✅ 매우 빠른 성능

**단점:**
- ❌ **복잡한 비즈니스 로직 처리 불가** (단일 테이블만 가능)
- ❌ JPA 엔티티와 동기화 문제
- ❌ 여러 테이블 연관 업데이트 불가

**적합한 경우:**
- 단순 카운터 증가 (조회수, 좋아요)
- 단일 값 업데이트
- 복잡한 검증 로직이 없는 경우

**부적합한 경우:**
- ❌ 쿠폰 발급 (Coupon + UserCoupon 두 테이블)
- ❌ 주문 생성 (Order + OrderItem + Product 여러 테이블)
- ❌ 복잡한 검증 로직이 필요한 경우

---

#### 2. 분산 락 (Distributed Lock)

**개념:** Redis 등을 이용한 애플리케이션 레벨 락

```java
@RedisLock(key = "#productId")
public void decreaseStock(Long productId, int quantity) {
    // 비즈니스 로직
}
```

**장점:**
- ✅ 다중 서버 환경에서 동작
- ✅ DB 락 부담 없음

**단점:**
- ❌ 인프라 복잡도 증가 (Redis 필요)
- ❌ 네트워크 지연
- ❌ 락 타임아웃 관리 필요

---

#### 3. 큐 시스템 (Queue-based)

**개념:** 요청을 큐에 넣고 순차 처리

```java
// Kafka, RabbitMQ 등
kafkaTemplate.send("stock-decrease", request);
```

**장점:**
- ✅ 비동기 처리로 응답 빠름
- ✅ 대용량 트래픽 처리 가능

**단점:**
- ❌ 즉시 결과 확인 불가
- ❌ 시스템 복잡도 대폭 증가
- ❌ 실패 처리 복잡

---

### 각 방법의 적용 시점

| 방법 | 적용 시점 |
|------|----------|
| **비관적 락** | ✅ **현재 프로젝트** (복잡한 로직, 높은 충돌) |
| 낙관적 락 | 충돌 적음, 읽기 많음 |
| 조건부 업데이트 | 단순 카운터, 단일 테이블 |
| 분산 락 | 다중 서버 환경 |
| 큐 시스템 | 초대용량 트래픽, 비동기 허용 가능 |

---

## 테스트 실행 방법

### 전체 테스트 실행
```bash
./gradlew test --tests "*LockMechanismComparisonTest"
```

### Docker 환경 준비
```bash
docker-compose up -d
```

### 개별 테스트 실행
```bash
# 비관적 락만
./gradlew test --tests "*LockMechanismComparisonTest.pessimisticLock_Concurrency"

# 낙관적 락만
./gradlew test --tests "*LockMechanismComparisonTest.optimisticLock_Concurrency_WithoutRetry"
```

---

## 참고 자료

- [JPA Lock Modes Documentation](https://docs.oracle.com/javaee/7/api/javax/persistence/LockModeType.html)
- [Hibernate Locking Guide](https://docs.jboss.org/hibernate/orm/6.6/userguide/html_single/Hibernate_User_Guide.html#locking)
- [MySQL InnoDB Locking](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html)
- [Optimistic vs Pessimistic Locking](https://vladmihalcea.com/optimistic-vs-pessimistic-locking/)

---

**작성일**: 2025-11-20
**작성자**: 프로젝트 팀
**버전**: 2.0 (락만 비교, 조건부 업데이트는 참고로 분리)
