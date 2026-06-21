ALTER TABLE payment
    ADD COLUMN approval_number VARCHAR(20)  NULL,
    ADD COLUMN card_type       VARCHAR(20)  NULL,
    ADD COLUMN card_number     VARCHAR(255) NULL,
    ADD COLUMN installment     VARCHAR(20)  NULL;
