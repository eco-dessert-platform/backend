-- settlement_item.status 컬럼 크기 확장
-- SettlementStatus enum 값 중 BEFORE_PAYMENT_CANCELLED(24자), AFTER_PAYMENT_CANCELLED(23자)가
-- 기존 VARCHAR(20) 제한을 초과하므로 VARCHAR(30)으로 확장한다.
ALTER TABLE settlement_item
    MODIFY COLUMN status VARCHAR(30);
