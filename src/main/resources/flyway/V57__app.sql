CREATE TABLE seller_vat_settlement
(
    id                                    BIGINT AUTO_INCREMENT,
    seller_id                             BIGINT       NOT NULL,
    settlement_date                       DATE         NOT NULL,
    settlement_no                         VARCHAR(50)  NOT NULL,
    taxable_sales_amount                  BIGINT       NOT NULL DEFAULT 0,
    tax_free_sales_amount                 BIGINT       NOT NULL DEFAULT 0,
    credit_card_amount                    BIGINT       NOT NULL DEFAULT 0,
    cash_receipt_income_deduction_amount  BIGINT       NOT NULL DEFAULT 0,
    cash_receipt_expense_proof_amount     BIGINT       NOT NULL DEFAULT 0,
    etc_amount                            BIGINT       NOT NULL DEFAULT 0,
    created_at                            DATETIME(6)  NULL,
    modified_at                           DATETIME(6)  NULL,
    CONSTRAINT seller_vat_settlement_pk PRIMARY KEY (id),
    CONSTRAINT uk_seller_vat_settlement_seller_no UNIQUE (seller_id, settlement_no),
    CONSTRAINT fk_seller_vat_settlement_seller FOREIGN KEY (seller_id) REFERENCES sellers (id)
);

CREATE INDEX idx_seller_vat_settlement_seller_date
    ON seller_vat_settlement (seller_id, settlement_date);
