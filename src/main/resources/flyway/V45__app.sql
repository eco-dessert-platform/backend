-- 1. pagination 핵심
CREATE INDEX idx_store_application_status_created_id ON store_application (status, created_at, id);
-- 2. accountVerification 서브쿼리
CREATE INDEX idx_account_verification_seller_verified_id ON account_verifications (seller_id, verified, id DESC);
-- 3. join 성능
CREATE INDEX idx_store_application_seller_id ON store_application (seller_id);