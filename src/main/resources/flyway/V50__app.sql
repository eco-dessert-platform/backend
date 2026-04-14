CREATE INDEX idx_store_application_status_created_id ON store_application (status, created_at, id);
CREATE INDEX idx_account_verification_seller_verified_id ON account_verifications (seller_id, verified, id DESC);
CREATE INDEX idx_store_application_seller_id ON store_application (seller_id);