-- ==================================================
-- EXPLAIN 테스트용 쿼리 모음
-- ==================================================
-- 사용법:
-- 1. 인덱스 없는 상태에서 각 쿼리 앞에 EXPLAIN을 붙여 실행
-- 2. 실행 계획의 type, rows, Extra 컬럼 확인
-- 3. 인덱스 추가 후 동일 쿼리로 성능 비교
-- ==================================================

-- ==================================================
-- 1. users 테이블 테스트
-- ==================================================

-- 1-1. public_id로 조회 (idx_public_id 인덱스 필요)
SELECT * FROM users WHERE public_id = (SELECT public_id FROM users LIMIT 1);

-- 1-2. email로 조회 (idx_email 인덱스 필요)
SELECT * FROM users WHERE email = 'hong@example.com';

-- 1-3. 잔액 범위 조회
SELECT * FROM users WHERE balance >= 100000 ORDER BY balance DESC;

-- 1-4. 이름 검색
SELECT * FROM users WHERE name LIKE '%홍%';

-- ==================================================
-- 2. categories 테이블 테스트
-- ==================================================

-- 2-1. 카테고리명으로 조회 (idx_name 인덱스 필요)
SELECT * FROM categories WHERE name = '전자제품';

-- 2-2. 카테고리별 상품 수 집계
SELECT c.name, COUNT(p.id) as product_count
FROM categories c
LEFT JOIN products p ON c.id = p.category_id
GROUP BY c.id, c.name;

-- ==================================================
-- 3. products 테이블 테스트
-- ==================================================

-- 3-1. category_id로 조회 (idx_category_id 인덱스 필요)
SELECT * FROM products WHERE category_id = 1;

-- 3-2. 상품명 검색 (idx_name 인덱스 필요)
SELECT * FROM products WHERE name LIKE '%노트북%';

-- 3-3. 가격 범위 조회
SELECT * FROM products WHERE price BETWEEN 10000 AND 100000;

-- 3-4. 재고가 있는 상품 조회
SELECT * FROM products WHERE stock > 0 ORDER BY stock DESC;

-- 3-5. 카테고리별 평균 가격
SELECT c.name, AVG(p.price) as avg_price, COUNT(p.id) as product_count
FROM categories c
JOIN products p ON c.id = p.category_id
GROUP BY c.id, c.name;

-- ==================================================
-- 4. shipping_addresses 테이블 테스트
-- ==================================================

-- 4-1. user_id로 조회 (idx_user_id 인덱스 필요)
SELECT * FROM shipping_addresses WHERE user_id = 1;

-- 4-2. 기본 배송지 조회 (idx_user_default 복합 인덱스 필요)
SELECT * FROM shipping_addresses WHERE user_id = 1 AND is_default = TRUE;

-- 4-3. 사용자별 배송지 개수
SELECT user_id, COUNT(*) as address_count
FROM shipping_addresses
GROUP BY user_id
HAVING COUNT(*) > 1;

-- ==================================================
-- 5. coupons 테이블 테스트
-- ==================================================

-- 5-1. 쿠폰 타입으로 조회 (idx_type 인덱스 필요)
SELECT * FROM coupons WHERE coupon_type = 'RATE';

-- 5-2. 유효기간 내 쿠폰 조회 (idx_dates 인덱스 필요)
SELECT * FROM coupons
WHERE start_date <= NOW() AND end_date >= NOW();

-- 5-3. 발급 가능한 쿠폰 조회
SELECT * FROM coupons
WHERE issued_quantity < total_quantity
AND start_date <= NOW()
AND end_date >= NOW();

-- 5-4. 대기열 사용 쿠폰 조회
SELECT * FROM coupons WHERE use_queue = TRUE;

-- ==================================================
-- 6. user_coupons 테이블 테스트
-- ==================================================

-- 6-1. 사용자별 사용 가능 쿠폰 조회 (idx_user_status 복합 인덱스 필요)
SELECT uc.*, c.name, c.discount_rate, c.discount_amount
FROM user_coupons uc
JOIN coupons c ON uc.coupon_id = c.id
WHERE uc.user_id = 1 AND uc.status = 'AVAILABLE';

-- 6-2. 쿠폰 ID로 발급 내역 조회 (idx_coupon_id 인덱스 필요)
SELECT * FROM user_coupons WHERE coupon_id = 1;

-- 6-3. 만료 예정 쿠폰 조회 (idx_expires_at 인덱스 필요)
SELECT uc.*, u.name, c.name as coupon_name
FROM user_coupons uc
JOIN users u ON uc.user_id = u.id
JOIN coupons c ON uc.coupon_id = c.id
WHERE uc.expires_at BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 7 DAY)
AND uc.status = 'AVAILABLE';

-- 6-4. 사용자별 쿠폰 사용 통계
SELECT user_id, status, COUNT(*) as count
FROM user_coupons
GROUP BY user_id, status;

-- ==================================================
-- 7. coupon_queues 테이블 테스트
-- ==================================================

-- 7-1. 쿠폰별 대기열 조회 (idx_coupon_status 복합 인덱스 필요)
SELECT * FROM coupon_queues
WHERE coupon_id = 7 AND status = 'PENDING'
ORDER BY queue_position;

-- 7-2. 사용자별 대기열 조회 (idx_user_coupon 복합 인덱스 필요)
SELECT * FROM coupon_queues
WHERE user_id = 1 AND coupon_id = 7;

-- 7-3. 대기 순번으로 조회 (idx_queue_position 인덱스 필요)
SELECT * FROM coupon_queues
WHERE queue_position <= 100
ORDER BY queue_position;

-- 7-4. 쿠폰별 대기 현황 통계
SELECT coupon_id, status, COUNT(*) as count, MIN(queue_position) as min_pos, MAX(queue_position) as max_pos
FROM coupon_queues
GROUP BY coupon_id, status;

-- ==================================================
-- 8. orders 테이블 테스트
-- ==================================================

-- 8-1. 주문번호로 조회 (idx_order_number 인덱스 필요)
SELECT * FROM orders WHERE order_number = (SELECT order_number FROM orders LIMIT 1);

-- 8-2. 사용자별 주문 조회 (idx_user_id 인덱스 필요)
SELECT * FROM orders WHERE user_id = 1 ORDER BY created_at DESC;

-- 8-3. 주문 상태별 조회 (idx_status 인덱스 필요)
SELECT * FROM orders WHERE status = 'DELIVERED';

-- 8-4. 날짜 범위로 조회 (idx_created_at 인덱스 필요)
SELECT * FROM orders
WHERE created_at BETWEEN '2025-01-01' AND '2025-01-31'
ORDER BY created_at DESC;

-- 8-5. 사용자별 주문 통계
SELECT user_id, COUNT(*) as order_count, SUM(final_amount) as total_amount
FROM orders
GROUP BY user_id
ORDER BY total_amount DESC;

-- 8-6. 상태별 주문 통계
SELECT status, COUNT(*) as count, SUM(final_amount) as total_amount
FROM orders
GROUP BY status;

-- 8-7. 일별 주문 통계
SELECT DATE(created_at) as order_date, COUNT(*) as order_count, SUM(final_amount) as daily_revenue
FROM orders
GROUP BY DATE(created_at)
ORDER BY order_date DESC;

-- ==================================================
-- 9. order_items 테이블 테스트
-- ==================================================

-- 9-1. 주문별 상품 조회 (idx_order_id 인덱스 필요)
SELECT oi.*, p.name, p.price
FROM order_items oi
JOIN products p ON oi.product_id = p.id
WHERE oi.order_id = 1;

-- 9-2. 상품별 판매 내역 (idx_product_id 인덱스 필요)
SELECT product_id, SUM(quantity) as total_sold, SUM(subtotal) as total_revenue
FROM order_items
GROUP BY product_id
ORDER BY total_sold DESC
LIMIT 20;

-- 9-3. 베스트셀러 상품 조회
SELECT p.name, p.category_id, SUM(oi.quantity) as total_sold
FROM order_items oi
JOIN products p ON oi.product_id = p.id
GROUP BY p.id, p.name, p.category_id
ORDER BY total_sold DESC
LIMIT 10;

-- ==================================================
-- 10. payments 테이블 테스트
-- ==================================================

-- 10-1. payment_id로 조회 (idx_payment_id 인덱스 필요)
SELECT * FROM payments WHERE payment_id = (SELECT payment_id FROM payments LIMIT 1);

-- 10-2. 주문별 결제 조회 (idx_order_id 인덱스 필요)
SELECT * FROM payments WHERE order_id = 1;

-- 10-3. 결제 상태별 조회 (idx_status 인덱스 필요)
SELECT * FROM payments WHERE status = 'COMPLETED';

-- 10-4. 데이터 전송 상태별 조회 (idx_data_transmission_status 인덱스 필요)
SELECT * FROM payments WHERE data_transmission_status = 'PENDING';

-- 10-5. 결제 완료 건수 및 금액 집계
SELECT status, COUNT(*) as count, SUM(paid_amount) as total_amount
FROM payments
GROUP BY status;

-- 10-6. 일별 결제 통계
SELECT DATE(created_at) as payment_date,
       COUNT(*) as payment_count,
       SUM(paid_amount) as daily_revenue
FROM payments
WHERE status = 'COMPLETED'
GROUP BY DATE(created_at)
ORDER BY payment_date DESC;

-- ==================================================
-- 11. cart_items 테이블 테스트
-- ==================================================

-- 11-1. 사용자별 장바구니 조회 (idx_user_id 인덱스 필요)
SELECT ci.*, p.name, p.price, (p.price * ci.quantity) as subtotal
FROM cart_items ci
JOIN products p ON ci.product_id = p.id
WHERE ci.user_id = 1;

-- 11-2. 상품별 장바구니 담김 수 (idx_product_id 인덱스 필요)
SELECT product_id, COUNT(*) as cart_count, SUM(quantity) as total_quantity
FROM cart_items
GROUP BY product_id
ORDER BY cart_count DESC;

-- 11-3. 특정 상품을 담은 사용자 조회 (UNIQUE KEY uk_user_product 활용)
SELECT ci.*, u.name, u.email
FROM cart_items ci
JOIN users u ON ci.user_id = u.id
WHERE ci.product_id = 1;

-- ==================================================
-- 12. refunds 테이블 테스트
-- ==================================================

-- 12-1. 주문별 환불 조회 (idx_order_id 인덱스 필요)
SELECT * FROM refunds WHERE order_id = 2;

-- 12-2. 환불 상태별 조회 (idx_status 인덱스 필요)
SELECT * FROM refunds WHERE status = 'COMPLETED';

-- 12-3. 환불 통계
SELECT status, COUNT(*) as count, SUM(refund_amount) as total_refund
FROM refunds
GROUP BY status;

-- 12-4. 환불 사유별 통계
SELECT reason, COUNT(*) as count, SUM(refund_amount) as total_amount
FROM refunds
GROUP BY reason
ORDER BY count DESC;

-- ==================================================
-- 13. outbox_events 테이블 테스트
-- ==================================================

-- 13-1. 상태별 이벤트 조회 (idx_status_retry 복합 인덱스 필요)
SELECT * FROM outbox_events
WHERE status = 'PENDING' AND retry_count < 3
ORDER BY created_at;

-- 13-2. 실패한 이벤트 조회
SELECT * FROM outbox_events
WHERE status = 'FAILED'
ORDER BY retry_count DESC;

-- 13-3. 날짜별 이벤트 조회 (idx_created_at 인덱스 필요)
SELECT * FROM outbox_events
WHERE created_at >= '2025-01-15 00:00:00'
ORDER BY created_at DESC;

-- 13-4. 이벤트 타입별 통계
SELECT event_type, status, COUNT(*) as count
FROM outbox_events
GROUP BY event_type, status
ORDER BY event_type, status;

-- 13-5. 처리 대기 중인 이벤트
SELECT * FROM outbox_events
WHERE status IN ('PENDING', 'PROCESSING')
ORDER BY created_at
LIMIT 100;

-- ==================================================
-- 14. popular_products 테이블 테스트
-- ==================================================

-- 14-1. 순위별 조회 (idx_rank 인덱스 필요)
SELECT * FROM popular_products
WHERE `rank` <= 10
ORDER BY `rank`;

-- 14-2. 최근 업데이트 조회 (idx_updated_at 인덱스 필요)
SELECT * FROM popular_products
WHERE updated_at >= DATE_SUB(NOW(), INTERVAL 1 DAY)
ORDER BY `rank`;

-- 14-3. 카테고리별 인기 상품
SELECT category_name, COUNT(*) as count, AVG(total_sales_quantity) as avg_sales
FROM popular_products
GROUP BY category_name;

-- ==================================================
-- 복합 쿼리 (JOIN이 많은 실전 쿼리)
-- ==================================================

-- 15-1. 사용자 주문 상세 정보 조회 (여러 인덱스 활용)
SELECT
    o.order_number,
    o.created_at,
    o.status,
    o.total_amount,
    o.discount_amount,
    o.final_amount,
    u.name as user_name,
    u.email as user_email,
    p.status as payment_status,
    p.payment_id,
    uc.status as coupon_status,
    c.name as coupon_name
FROM orders o
JOIN users u ON o.user_id = u.id
LEFT JOIN payments p ON o.id = p.order_id
LEFT JOIN user_coupons uc ON o.user_coupon_id = uc.id
LEFT JOIN coupons c ON uc.coupon_id = c.id
WHERE o.user_id = 1
ORDER BY o.created_at DESC;

-- 15-2. 주문 전체 상세 (주문 + 주문상품 + 상품 + 결제)
SELECT
    o.order_number,
    o.status as order_status,
    o.created_at,
    oi.quantity,
    oi.unit_price,
    oi.subtotal,
    p.name as product_name,
    c.name as category_name,
    py.status as payment_status,
    py.payment_id
FROM orders o
JOIN order_items oi ON o.id = oi.order_id
JOIN products p ON oi.product_id = p.id
JOIN categories c ON p.category_id = c.id
JOIN payments py ON o.id = py.order_id
WHERE o.id = 1;

-- 15-3. 사용자별 구매 패턴 분석
SELECT
    u.name,
    u.email,
    COUNT(DISTINCT o.id) as total_orders,
    SUM(o.final_amount) as total_spent,
    AVG(o.final_amount) as avg_order_amount,
    COUNT(DISTINCT oi.product_id) as unique_products
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
LEFT JOIN order_items oi ON o.id = oi.order_id
GROUP BY u.id, u.name, u.email
HAVING total_orders > 0
ORDER BY total_spent DESC;

-- 15-4. 카테고리별 매출 통계
SELECT
    c.name as category_name,
    COUNT(DISTINCT p.id) as product_count,
    COUNT(DISTINCT oi.order_id) as order_count,
    SUM(oi.quantity) as total_quantity_sold,
    SUM(oi.subtotal) as total_revenue
FROM categories c
JOIN products p ON c.id = p.category_id
LEFT JOIN order_items oi ON p.id = oi.product_id
GROUP BY c.id, c.name
ORDER BY total_revenue DESC;

-- 15-5. 쿠폰 사용 효과 분석
SELECT
    c.name as coupon_name,
    c.coupon_type,
    c.discount_rate,
    c.discount_amount,
    COUNT(DISTINCT uc.id) as issued_count,
    COUNT(DISTINCT CASE WHEN uc.status = 'USED' THEN uc.id END) as used_count,
    COUNT(DISTINCT o.id) as order_count,
    SUM(o.discount_amount) as total_discount_given,
    SUM(o.final_amount) as total_revenue_with_coupon
FROM coupons c
LEFT JOIN user_coupons uc ON c.id = uc.coupon_id
LEFT JOIN orders o ON uc.id = o.user_coupon_id
GROUP BY c.id, c.name, c.coupon_type, c.discount_rate, c.discount_amount
ORDER BY issued_count DESC;

-- ==================================================
-- 성능 테스트 핵심 쿼리
-- ==================================================

-- 16-1. 인덱스 효과가 큰 쿼리: user_id + status 복합 조건
SELECT * FROM orders
WHERE user_id = 1 AND status = 'DELIVERED'
ORDER BY created_at DESC;

-- 16-2. 인덱스 효과가 큰 쿼리: 범위 검색 + 정렬
SELECT * FROM products
WHERE category_id = 1 AND price BETWEEN 50000 AND 500000
ORDER BY price DESC;

-- 16-3. 인덱스 효과가 큰 쿼리: JOIN + WHERE + GROUP BY
SELECT
    p.name,
    COUNT(oi.id) as order_count,
    SUM(oi.quantity) as total_sold
FROM products p
JOIN order_items oi ON p.id = oi.product_id
WHERE p.category_id = 1
GROUP BY p.id, p.name
ORDER BY total_sold DESC;

-- 16-4. 인덱스 효과가 큰 쿼리: 서브쿼리
SELECT * FROM orders o
WHERE o.id IN (
    SELECT DISTINCT order_id
    FROM order_items
    WHERE product_id = 1
);

-- 16-5. 인덱스 효과가 큰 쿼리: 상관 서브쿼리
SELECT u.name, u.email,
    (SELECT COUNT(*) FROM orders WHERE user_id = u.id) as order_count,
    (SELECT SUM(final_amount) FROM orders WHERE user_id = u.id) as total_spent
FROM users u
WHERE u.balance > 100000;

-- ==================================================
-- 테스트 방법
-- ==================================================
--
-- 1. 각 쿼리 앞에 EXPLAIN 키워드를 붙여 실행:
--    EXPLAIN SELECT * FROM users WHERE email = 'hong@example.com';
--
-- 2. 실행 계획에서 확인할 주요 항목:
--    - type: 조인 타입 (ALL이면 풀스캔, ref/eq_ref이면 인덱스 사용)
--    - possible_keys: 사용 가능한 인덱스 목록
--    - key: 실제 사용된 인덱스
--    - rows: 검사할 예상 행 수 (적을수록 좋음)
--    - Extra: 추가 정보 (Using where, Using index, Using filesort 등)
--
-- 3. 인덱스 전/후 비교:
--    - 인덱스 없을 때: type=ALL, rows=많음, key=NULL
--    - 인덱스 있을 때: type=ref, rows=적음, key=인덱스명
--
-- ==================================================
