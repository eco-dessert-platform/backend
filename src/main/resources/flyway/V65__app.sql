-- order_group_id NOT NULL 제약 추가
ALTER TABLE orders MODIFY order_group_id VARCHAR(30) NOT NULL;

-- order_number UNIQUE 제약 추가
ALTER TABLE orders ADD UNIQUE KEY uk_order_number (order_number);
