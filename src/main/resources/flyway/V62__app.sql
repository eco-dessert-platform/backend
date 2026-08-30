-- 구매확정 시각 컬럼 추가 (수동 확정 / 배송완료 후 7일 자동확정 시 기록)
ALTER TABLE order_item
    ADD COLUMN purchase_confirmed_at DATETIME(6) NULL AFTER delivery_status;
