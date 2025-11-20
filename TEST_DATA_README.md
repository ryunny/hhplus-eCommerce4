# 테스트 데이터 생성 가이드

## 📋 개요

인덱스 성능 테스트를 위한 대용량 테스트 데이터 생성 스크립트입니다.

## 📊 생성되는 데이터

| 테이블 | 건수 | 설명 |
|--------|------|------|
| users | 10,000 | 사용자 |
| categories | 100 | 카테고리 |
| products | 10,000 | 상품 |
| shipping_addresses | 10,000 | 배송지 |
| coupons | 1,000 | 쿠폰 |
| user_coupons | 5,000 | 사용자 쿠폰 |
| orders | 10,000 | 주문 |
| order_items | 30,000 | 주문 상품 (주문당 평균 3개) |
| payments | 10,000 | 결제 |
| cart_items | 5,000 | 장바구니 |
| popular_products | 100 | 인기 상품 |
| **총계** | **약 85,200건** | |

## 🚀 실행 방법

### 1. 인덱스 제거 (선택사항)

인덱스 없는 상태에서 성능 테스트하려면:

```bash
mysql -u ecommerce_user -p ecommerce < drop_custom_indexes.sql
```

### 2. 테스트 데이터 생성

```bash
# Docker MySQL 접속
docker exec -it ecommerce-mysql mysql -u ecommerce_user -p ecommerce

# 또는 파일 실행
mysql -u ecommerce_user -p ecommerce < generate_test_data.sql
```

### 3. 프로시저 실행

MySQL 접속 후:

```sql
-- 전체 테스트 데이터 생성 (약 2-3분 소요)
CALL generate_all_test_data();
```

**또는 개별 테이블만 생성:**

```sql
-- 사용자만
CALL generate_users();

-- 상품만
CALL generate_products();

-- 주문만
CALL generate_orders();
```

### 4. 데이터 확인

```sql
SELECT
    'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'products', COUNT(*) FROM products
UNION ALL
SELECT 'orders', COUNT(*) FROM orders
UNION ALL
SELECT 'order_items', COUNT(*) FROM order_items;
```

## ⏱️ 예상 실행 시간

- **전체 데이터 생성**: 약 2~3분
- **users (만 건)**: 약 20초
- **products (만 건)**: 약 20초
- **orders (만 건)**: 약 20초
- **order_items (3만 건)**: 약 60초

## 🔍 인덱스 성능 테스트

### 인덱스 없이 쿼리 실행

```sql
-- 인덱스 제거
SOURCE drop_custom_indexes.sql;

-- 쿼리 성능 측정
EXPLAIN ANALYZE
SELECT * FROM products WHERE category_id = 10;

EXPLAIN ANALYZE
SELECT * FROM orders WHERE user_id = 100 AND status = 'DELIVERED';
```

### 인덱스 추가 후 비교

```sql
-- 인덱스 재생성
CREATE INDEX idx_category_id ON products(category_id);
CREATE INDEX idx_user_id ON orders(user_id);
CREATE INDEX idx_status ON orders(status);

-- 동일 쿼리 다시 실행
EXPLAIN ANALYZE
SELECT * FROM products WHERE category_id = 10;

EXPLAIN ANALYZE
SELECT * FROM orders WHERE user_id = 100 AND status = 'DELIVERED';
```

## 🗑️ 데이터 삭제

테스트 완료 후:

```sql
-- 모든 데이터 삭제
TRUNCATE TABLE popular_products;
TRUNCATE TABLE cart_items;
TRUNCATE TABLE refunds;
TRUNCATE TABLE payments;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE coupon_queues;
TRUNCATE TABLE user_coupons;
TRUNCATE TABLE shipping_addresses;
TRUNCATE TABLE coupons;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
TRUNCATE TABLE users;
```

## 📝 주의사항

1. **외래 키 제약조건**: schema.sql에서 `FOREIGN KEY` 제약조건이 `NO CONSTRAINT`로 설정되어 있어 참조 무결성 검사를 하지 않습니다.
2. **Auto Increment 리셋**: TRUNCATE 후 AUTO_INCREMENT가 1부터 다시 시작됩니다.
3. **디스크 공간**: 약 500MB~1GB 정도 필요합니다.
4. **실행 환경**: 로컬 개발 환경에서만 사용하세요. 운영 환경에서는 절대 실행하지 마세요!

## 🔧 트러블슈팅

### 프로시저 생성 실패

```sql
-- 기존 프로시저 확인
SHOW PROCEDURE STATUS WHERE Db = 'ecommerce';

-- 프로시저 삭제
DROP PROCEDURE IF EXISTS generate_all_test_data;
```

### 실행 시간 초과

```sql
-- 타임아웃 설정 증가
SET SESSION max_execution_time = 0;
SET SESSION wait_timeout = 28800;
```

### 메모리 부족

배치 크기를 줄이세요:
```sql
-- generate_test_data.sql 파일에서
DECLARE batch_size INT DEFAULT 1000;
-- 를
DECLARE batch_size INT DEFAULT 500;
-- 로 변경
```

## 📂 파일 구조

```
ecommerce/
├── schema.sql                    # 원본 스키마 (인덱스 포함)
├── drop_custom_indexes.sql      # 추가 인덱스 제거
├── generate_test_data.sql       # 테스트 데이터 생성 프로시저
└── TEST_DATA_README.md          # 이 파일
```
