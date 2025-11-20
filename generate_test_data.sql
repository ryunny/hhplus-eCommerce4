-- ==================================================
-- 테스트 데이터 생성 스크립트 (MySQL 프로시저)
-- 각 테이블에 약 만 건의 데이터 생성
-- ==================================================

DELIMITER $$

-- ==================================================
-- 1. users 테이블 (10,000건)
-- ==================================================
DROP PROCEDURE IF EXISTS generate_users$$
CREATE PROCEDURE generate_users()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE batch_size INT DEFAULT 1000;

    WHILE i <= 10000 DO
        INSERT INTO users (public_id, name, email, phone, balance, created_at)
        VALUES (
            UUID(),
            CONCAT('User', i),
            CONCAT('user', i, '@test.com'),
            CONCAT('010-', LPAD(FLOOR(RAND() * 10000), 4, '0'), '-', LPAD(FLOOR(RAND() * 10000), 4, '0')),
            FLOOR(RAND() * 1000000),
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY)
        );

        SET i = i + 1;

        IF i % batch_size = 0 THEN
            COMMIT;
        END IF;
    END WHILE;
    COMMIT;
END$$

-- ==================================================
-- 2. categories 테이블 (100건)
-- ==================================================
DROP PROCEDURE IF EXISTS generate_categories$$
CREATE PROCEDURE generate_categories()
BEGIN
    DECLARE i INT DEFAULT 1;

    WHILE i <= 100 DO
        INSERT INTO categories (name, created_at)
        VALUES (
            CONCAT('Category', i),
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY)
        );
        SET i = i + 1;
    END WHILE;
    COMMIT;
END$$

-- ==================================================
-- 3. products 테이블 (10,000건)
-- ==================================================
DROP PROCEDURE IF EXISTS generate_products$$
CREATE PROCEDURE generate_products()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE batch_size INT DEFAULT 1000;
    DECLARE category_count INT;

    SELECT COUNT(*) INTO category_count FROM categories;

    WHILE i <= 10000 DO
        INSERT INTO products (category_id, name, description, price, stock, version, created_at)
        VALUES (
            FLOOR(RAND() * category_count) + 1,
            CONCAT('Product', i),
            CONCAT('Description for Product', i),
            FLOOR(RAND() * 100000) + 1000,
            FLOOR(RAND() * 1000),
            0,
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY)
        );

        SET i = i + 1;

        IF i % batch_size = 0 THEN
            COMMIT;
        END IF;
    END WHILE;
    COMMIT;
END$$

-- ==================================================
-- 4. shipping_addresses 테이블 (10,000건)
-- ==================================================
DROP PROCEDURE IF EXISTS generate_shipping_addresses$$
CREATE PROCEDURE generate_shipping_addresses()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE batch_size INT DEFAULT 1000;

    WHILE i <= 10000 DO
        INSERT INTO shipping_addresses (user_id, address_name, recipient_name, address, phone, is_default, created_at)
        VALUES (
            i,
            CONCAT('Home', i),
            CONCAT('Recipient', i),
            CONCAT('Seoul, Gangnam-gu, Street ', i),
            CONCAT('010-', LPAD(FLOOR(RAND() * 10000), 4, '0'), '-', LPAD(FLOOR(RAND() * 10000), 4, '0')),
            IF(RAND() > 0.5, TRUE, FALSE),
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY)
        );

        SET i = i + 1;

        IF i % batch_size = 0 THEN
            COMMIT;
        END IF;
    END WHILE;
    COMMIT;
END$$

-- ==================================================
-- 5. coupons 테이블 (1,000건)
-- ==================================================
DROP PROCEDURE IF EXISTS generate_coupons$$
CREATE PROCEDURE generate_coupons()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE batch_size INT DEFAULT 100;

    WHILE i <= 1000 DO
        INSERT INTO coupons (name, coupon_type, discount_rate, discount_amount, min_order_amount,
                           total_quantity, issued_quantity, start_date, end_date, use_queue)
        VALUES (
            CONCAT('Coupon', i),
            IF(RAND() > 0.5, 'PERCENTAGE', 'FIXED'),
            IF(RAND() > 0.5, FLOOR(RAND() * 30) + 5, NULL),
            IF(RAND() > 0.5, FLOOR(RAND() * 10000) + 1000, NULL),
            FLOOR(RAND() * 50000),
            FLOOR(RAND() * 1000) + 100,
            FLOOR(RAND() * 50),
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY),
            DATE_ADD(NOW(), INTERVAL FLOOR(RAND() * 90) DAY),
            IF(RAND() > 0.8, TRUE, FALSE)
        );

        SET i = i + 1;

        IF i % batch_size = 0 THEN
            COMMIT;
        END IF;
    END WHILE;
    COMMIT;
END$$

-- ==================================================
-- 6. user_coupons 테이블 (5,000건)
-- ==================================================
DROP PROCEDURE IF EXISTS generate_user_coupons$$
CREATE PROCEDURE generate_user_coupons()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE batch_size INT DEFAULT 1000;
    DECLARE user_count INT;
    DECLARE coupon_count INT;

    SELECT COUNT(*) INTO user_count FROM users;
    SELECT COUNT(*) INTO coupon_count FROM coupons;

    WHILE i <= 5000 DO
        INSERT INTO user_coupons (user_id, coupon_id, status, expires_at)
        VALUES (
            FLOOR(RAND() * user_count) + 1,
            FLOOR(RAND() * coupon_count) + 1,
            ELT(FLOOR(RAND() * 3) + 1, 'AVAILABLE', 'USED', 'EXPIRED'),
            DATE_ADD(NOW(), INTERVAL FLOOR(RAND() * 90) DAY)
        );

        SET i = i + 1;

        IF i % batch_size = 0 THEN
            COMMIT;
        END IF;
    END WHILE;
    COMMIT;
END$$

-- ==================================================
-- 7. orders 테이블 (10,000건)
-- ==================================================
DROP PROCEDURE IF EXISTS generate_orders$$
CREATE PROCEDURE generate_orders()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE batch_size INT DEFAULT 1000;
    DECLARE user_count INT;

    SELECT COUNT(*) INTO user_count FROM users;

    WHILE i <= 10000 DO
        INSERT INTO orders (order_number, user_id, status, total_amount, discount_amount,
                          final_amount, address, recipient_name, shipping_phone, created_at)
        VALUES (
            UUID(),
            FLOOR(RAND() * user_count) + 1,
            ELT(FLOOR(RAND() * 6) + 1, 'PENDING', 'PAID', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'),
            FLOOR(RAND() * 500000) + 10000,
            FLOOR(RAND() * 50000),
            FLOOR(RAND() * 500000) + 10000,
            CONCAT('Seoul, Gangnam-gu, Street ', i),
            CONCAT('Recipient', i),
            CONCAT('010-', LPAD(FLOOR(RAND() * 10000), 4, '0'), '-', LPAD(FLOOR(RAND() * 10000), 4, '0')),
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY)
        );

        SET i = i + 1;

        IF i % batch_size = 0 THEN
            COMMIT;
        END IF;
    END WHILE;
    COMMIT;
END$$

-- ==================================================
-- 8. order_items 테이블 (30,000건)
-- ==================================================
DROP PROCEDURE IF EXISTS generate_order_items$$
CREATE PROCEDURE generate_order_items()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE batch_size INT DEFAULT 1000;
    DECLARE order_count INT;
    DECLARE product_count INT;
    DECLARE qty INT;
    DECLARE unit_price BIGINT;

    SELECT COUNT(*) INTO order_count FROM orders;
    SELECT COUNT(*) INTO product_count FROM products;

    WHILE i <= 30000 DO
        SET qty = FLOOR(RAND() * 5) + 1;
        SET unit_price = FLOOR(RAND() * 100000) + 1000;

        INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal, created_at)
        VALUES (
            FLOOR(RAND() * order_count) + 1,
            FLOOR(RAND() * product_count) + 1,
            qty,
            unit_price,
            qty * unit_price,
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY)
        );

        SET i = i + 1;

        IF i % batch_size = 0 THEN
            COMMIT;
        END IF;
    END WHILE;
    COMMIT;
END$$

-- ==================================================
-- 9. payments 테이블 (10,000건)
-- ==================================================
DROP PROCEDURE IF EXISTS generate_payments$$
CREATE PROCEDURE generate_payments()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE batch_size INT DEFAULT 1000;

    WHILE i <= 10000 DO
        INSERT INTO payments (payment_id, order_id, status, data_transmission_status,
                            paid_amount, created_at)
        VALUES (
            UUID(),
            i,
            ELT(FLOOR(RAND() * 4) + 1, 'PENDING', 'COMPLETED', 'FAILED', 'REFUNDED'),
            ELT(FLOOR(RAND() * 3) + 1, 'PENDING', 'SUCCESS', 'FAILED'),
            FLOOR(RAND() * 500000) + 10000,
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY)
        );

        SET i = i + 1;

        IF i % batch_size = 0 THEN
            COMMIT;
        END IF;
    END WHILE;
    COMMIT;
END$$

-- ==================================================
-- 10. cart_items 테이블 (5,000건)
-- ==================================================
DROP PROCEDURE IF EXISTS generate_cart_items$$
CREATE PROCEDURE generate_cart_items()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE batch_size INT DEFAULT 1000;
    DECLARE user_count INT;
    DECLARE product_count INT;

    SELECT COUNT(*) INTO user_count FROM users;
    SELECT COUNT(*) INTO product_count FROM products;

    WHILE i <= 5000 DO
        INSERT INTO cart_items (user_id, product_id, quantity, created_at)
        VALUES (
            FLOOR(RAND() * user_count) + 1,
            FLOOR(RAND() * product_count) + 1,
            FLOOR(RAND() * 10) + 1,
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY)
        );

        SET i = i + 1;

        IF i % batch_size = 0 THEN
            COMMIT;
        END IF;
    END WHILE;
    COMMIT;
END$$

-- ==================================================
-- 11. popular_products 테이블 (100건)
-- ==================================================
DROP PROCEDURE IF EXISTS generate_popular_products$$
CREATE PROCEDURE generate_popular_products()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE product_count INT;

    SELECT COUNT(*) INTO product_count FROM products;

    WHILE i <= 100 DO
        INSERT INTO popular_products (product_id, view_count, order_count, updated_at)
        VALUES (
            FLOOR(RAND() * product_count) + 1,
            FLOOR(RAND() * 10000),
            FLOOR(RAND() * 1000),
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 7) DAY)
        );
        SET i = i + 1;
    END WHILE;
    COMMIT;
END$$

-- ==================================================
-- 실행 프로시저
-- ==================================================
DROP PROCEDURE IF EXISTS generate_all_test_data$$
CREATE PROCEDURE generate_all_test_data()
BEGIN
    SELECT 'Generating users...' AS status;
    CALL generate_users();

    SELECT 'Generating categories...' AS status;
    CALL generate_categories();

    SELECT 'Generating products...' AS status;
    CALL generate_products();

    SELECT 'Generating shipping_addresses...' AS status;
    CALL generate_shipping_addresses();

    SELECT 'Generating coupons...' AS status;
    CALL generate_coupons();

    SELECT 'Generating user_coupons...' AS status;
    CALL generate_user_coupons();

    SELECT 'Generating orders...' AS status;
    CALL generate_orders();

    SELECT 'Generating order_items...' AS status;
    CALL generate_order_items();

    SELECT 'Generating payments...' AS status;
    CALL generate_payments();

    SELECT 'Generating cart_items...' AS status;
    CALL generate_cart_items();

    SELECT 'Generating popular_products...' AS status;
    CALL generate_popular_products();

    SELECT 'All test data generated successfully!' AS status;
END$$

DELIMITER ;

-- ==================================================
-- 실행 방법:
-- CALL generate_all_test_data();
-- ==================================================
