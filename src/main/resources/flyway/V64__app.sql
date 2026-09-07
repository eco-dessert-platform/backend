ALTER TABLE orders ADD COLUMN order_group_id VARCHAR(30) NULL;
CREATE INDEX idx_order_group_id ON orders(order_group_id);
