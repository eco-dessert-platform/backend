ALTER TABLE product ADD COLUMN store_id BIGINT;

UPDATE product p
JOIN product_board pb ON pb.id = p.product_board_id
SET p.store_id = pb.store_id
WHERE p.store_id IS NULL;

ALTER TABLE product MODIFY store_id BIGINT NOT NULL;

ALTER TABLE product
ADD CONSTRAINT fk_product_store
FOREIGN KEY (store_id) REFERENCES store(id);