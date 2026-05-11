CREATE TABLE payment_hold (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    settlement_item_id BIGINT NOT NULL,
    settlement_number VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    base_date DATE NOT NULL,
    completed_date DATE,
    settlement_amount DECIMAL(15,2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    modified_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_payment_hold_seller FOREIGN KEY (seller_id) REFERENCES sellers (id),
    CONSTRAINT fk_payment_hold_settlement_item FOREIGN KEY (settlement_item_id) REFERENCES settlement_item (id)
);

CREATE UNIQUE INDEX uk_payment_hold_settlement_item_id
    ON payment_hold (settlement_item_id);

CREATE INDEX idx_payment_hold_seller_base_date
    ON payment_hold (seller_id, base_date);

CREATE INDEX idx_payment_hold_seller_settlement_number
    ON payment_hold (seller_id, settlement_number);

CREATE INDEX idx_payment_hold_seller_status_base_date
    ON payment_hold (seller_id, status, base_date);
