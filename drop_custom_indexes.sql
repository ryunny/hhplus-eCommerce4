-- ==================================================
-- 추가된 인덱스 삭제 스크립트
-- PK, UNIQUE 제약조건으로 자동 생성된 인덱스는 유지
-- ==================================================

-- categories
DROP INDEX idx_name ON categories;

-- products
DROP INDEX idx_category_id ON products;
DROP INDEX idx_name ON products;

-- shipping_addresses
DROP INDEX idx_user_id ON shipping_addresses;
DROP INDEX idx_user_default ON shipping_addresses;

-- coupons
DROP INDEX idx_dates ON coupons;
DROP INDEX idx_type ON coupons;

-- user_coupons
DROP INDEX idx_user_status ON user_coupons;
DROP INDEX idx_coupon_id ON user_coupons;
DROP INDEX idx_expires_at ON user_coupons;

-- coupon_queues
DROP INDEX idx_coupon_status ON coupon_queues;
DROP INDEX idx_user_coupon ON coupon_queues;
DROP INDEX idx_queue_position ON coupon_queues;

-- orders
DROP INDEX idx_user_id ON orders;
DROP INDEX idx_status ON orders;
DROP INDEX idx_created_at ON orders;

-- order_items
DROP INDEX idx_order_id ON order_items;
DROP INDEX idx_product_id ON order_items;

-- payments
DROP INDEX idx_order_id ON payments;
DROP INDEX idx_status ON payments;
DROP INDEX idx_data_transmission_status ON payments;

-- cart_items
DROP INDEX idx_user_id ON cart_items;
DROP INDEX idx_product_id ON cart_items;

-- refunds
DROP INDEX idx_order_id ON refunds;
DROP INDEX idx_status ON refunds;

-- outbox_events
DROP INDEX idx_status_retry ON outbox_events;
DROP INDEX idx_created_at ON outbox_events;

-- popular_products
DROP INDEX idx_updated_at ON popular_products;
