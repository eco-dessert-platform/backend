-- 1. Orders 테이블
ALTER TABLE orders ADD COLUMN seller_id BIGINT NULL AFTER member_id;
CREATE INDEX seller_id ON orders (seller_id);
ALTER TABLE orders ADD CONSTRAINT fk_orders_seller FOREIGN KEY (seller_id) REFERENCES sellers (id);

-- 2. Sellers 테이블
ALTER TABLE sellers ADD CONSTRAINT uk_sellers_store_id UNIQUE (store_id);

