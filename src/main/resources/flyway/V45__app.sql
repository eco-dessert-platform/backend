ALTER TABLE account_verifications
    ADD CONSTRAINT uq_account_verifications_seller_id UNIQUE (seller_id);
