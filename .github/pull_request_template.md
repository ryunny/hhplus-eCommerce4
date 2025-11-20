## :pushpin: PR 제목 규칙
[STEP11] 이하륜 - 동시성 제어 및 인덱스 성능 최적화

---

## :clipboard: 핵심 체크리스트 :white_check_mark:

### STEP09 - Concurrency (2개)
- [x] 애플리케이션 내에서 발생 가능한 **동시성 문제를 식별**했는가?
  - 재고 감소(Product.decreaseStock) 동시성 문제 식별
  - 100명이 동시에 재고 10개 상품 구매 시 음수 재고 발생 가능

- [x] 보고서에 DB를 활용한 **동시성 문제 해결 방안**이 포함되어 있는가?
  - 비관적 락(`PESSIMISTIC_WRITE`) vs 낙관적 락(`@Version`) 비교 분석
  - 실제 테스트 결과 기반 비관적 락 선택
  - 문서: `LOCK_MECHANISM_COMPARISON.md`

---

### STEP10 - Finalize (1개)
- [x] **동시성 문제를 드러낼 수 있는 통합 테스트**를 작성했는가?
  - `LockMechanismComparisonTest.java`
  - 100 스레드가 재고 10개 상품 동시 구매 테스트
  - 비관적 락: 10명 성공 / 90명 실패 (정확성 100%)
  - 낙관적 락: 3-5명 성공 / 95%+ 충돌 (재시도 필요)

---

## 📋 작업 내용

### 1. 동시성 제어 구현 및 비교 분석

#### 1-1. 비관적 락 (PESSIMISTIC_WRITE) 적용
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Optional<Product> findByIdWithLock(@Param("id") Long id);
```
- `SELECT ... FOR UPDATE`로 행 레벨 락 획득
- 다른 트랜잭션은 락 해제까지 대기
- 정확성 보장, 데이터 무결성 우선

#### 1-2. 낙관적 락 (@Version) 비교
```java
@Version
private Long version;
```
- 커밋 시점에 version 충돌 감지
- 95% 이상 충돌 발생 → 재시도 로직 필수
- 경합이 적을 때 유리

#### 1-3. 테스트 결과 (100 스레드, 재고 10개)
| 락 메커니즘 | 성공 | 실패 | 평균 시간 | 정확성 |
|------------|------|------|----------|--------|
| 비관적 락 | 10 | 90 | ~100ms | 100% |
| 낙관적 락 | 3-5 | 95+ | ~1ms | 재시도 필요 |

#### 1-4. 결론: 비관적 락 선택
- e-commerce 특성상 데이터 정확성이 최우선
- 재고 부족 에러 > 음수 재고 발생
- 성능보다 무결성 우선

---

### 2. 인덱스 성능 최적화 (추가 작업)

#### 2-1. 대용량 테스트 데이터 생성
- MySQL 프로시저 방식으로 81,100건 생성
- users(10k), orders(10k), order_items(30k), payments(10k) 등
- 실제 비즈니스 로직 기반 데이터

#### 2-2. 인덱스 성능 테스트 (27개 쿼리)
```sql
-- 인덱스 없이 실행
EXPLAIN ANALYZE SELECT * FROM orders WHERE user_id = 1;
-- 5.57ms (10,000 rows 풀스캔)

-- 인덱스 생성 후
CREATE INDEX idx_user_id ON orders(user_id);
EXPLAIN ANALYZE SELECT * FROM orders WHERE user_id = 1;
-- 0.0661ms (4 rows 인덱스 스캔) → 84배 개선
```

#### 2-3. TOP 5 극적인 성능 개선
1. **장바구니 상품별 집계**: 6.85ms → 0.0431ms (**159배**)
2. **주문 상세 조회**: 8.37ms → 0.0779ms (**107배**)
3. **배송지 목록 조회**: 4.62ms → 0.0475ms (**97배**)
4. **사용자 쿠폰 조회**: 1.33ms → 0.014ms (**95배**)
5. **결제 정보 조회**: 2.65ms → 0.0304ms (**87배**)

#### 2-4. 핵심 인사이트
- **복합 인덱스 > 단일 인덱스 2개**: `idx_user_status(user_id, status)` → 95배 개선
- **FK 컬럼은 무조건 인덱스**: JOIN 기준 컬럼 → 107배 개선
- **UNIQUE 키 확인 필수**: 중복 인덱스 방지
- **옵티마이저 신뢰**: 선택도 높은 인덱스 자동 선택

#### 2-5. 실무 적용 권장 인덱스
```sql
-- 즉시 적용 필수 (비용 대비 효과 최대)
CREATE INDEX idx_user_id ON orders(user_id);                    -- 84배
CREATE INDEX idx_order_id ON order_items(order_id);             -- 107배
CREATE INDEX idx_order_id ON payments(order_id);                -- 87배
CREATE INDEX idx_user_id ON shipping_addresses(user_id);        -- 97배
CREATE INDEX idx_user_status ON user_coupons(user_id, status);  -- 95배 (복합)
```

---

## 📁 주요 파일

### 동시성 제어
- `Product.java`: @Version 필드 추가
- `ProductJpaRepository.java`: findByIdWithLock() 추가
- `LockMechanismComparisonTest.java`: 통합 테스트
- `LOCK_MECHANISM_COMPARISON.md`: 비교 분석 문서

### 인덱스 최적화
- `generate_test_data.sql`: 81,100건 데이터 생성 프로시저
- `drop_custom_indexes.sql`: 인덱스 제거 스크립트
- `explain_test_queries.sql`: 30개 인덱스 비교 테스트 쿼리
- `query_test_result`: 27개 쿼리 성능 분석 보고서
- `TEST_DATA_README.md`: 사용 가이드

---

## 🎯 테스트 방법

### 동시성 테스트
```bash
./gradlew test --tests LockMechanismComparisonTest
```

### 인덱스 성능 테스트
```bash
# 1. 테스트 데이터 생성
docker exec -it ecommerce-mysql mysql -u ecommerce_user -p ecommerce
source generate_test_data.sql;
CALL generate_all_test_data();

# 2. 인덱스 제거
source drop_custom_indexes.sql;

# 3. explain_test_queries.sql의 쿼리 실행
# [1단계] 인덱스 없이 → [2단계] 인덱스 생성 → [3단계] 인덱스 있을 때
```

---

## ✍️ 간단 회고 (3줄 이내)
- **잘한 점**: 락 메커니즘 비교를 100 스레드 실제 테스트로 검증하고, 81,100건 데이터로 27개 쿼리 인덱스 성능을 측정(최대 159배 개선)하여 추측이 아닌 측정 기반으로 의사결정할 수 있었습니다.
- **어려웠던 점**: 낙관적 락이 95% 이상 충돌하는 상황에서 재시도 전략 설계가 복잡했고, 복합 인덱스 순서와 옵티마이저 선택을 이해하는데 시간이 걸렸습니다.
- **다음 시도**: Covering Index로 테이블 접근을 제거하고, 슬로우 쿼리 로그로 실제 프로덕션 쿼리 패턴을 분석하여 인덱스를 추가 최적화하겠습니다. 