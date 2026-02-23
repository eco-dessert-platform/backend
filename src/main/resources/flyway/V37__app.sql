-- V37__app.sql
-- 1. Orders 테이블
ALTER TABLE orders ADD COLUMN IF NOT EXISTS seller_id BIGINT NULL AFTER member_id;
CREATE INDEX IF NOT EXISTS seller_id ON orders (seller_id);

-- 기존 제약조건이 있다면 삭제
ALTER TABLE orders DROP FOREIGN KEY IF EXISTS fk_orders_seller;
ALTER TABLE orders ADD CONSTRAINT fk_orders_seller
    FOREIGN KEY (seller_id) REFERENCES sellers (id);

-- 2. Sellers 테이블
ALTER TABLE sellers DROP INDEX IF EXISTS uk_sellers_store_id;
ALTER TABLE sellers ADD CONSTRAINT uk_sellers_store_id UNIQUE (store_id);