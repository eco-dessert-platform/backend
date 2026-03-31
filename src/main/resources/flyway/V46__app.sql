-- 일별 정산 테이블에 판매자 FK, 정산번호, 공제/환급 상세 컬럼 추가

-- 판매자 FK
ALTER TABLE daily_settlement ADD COLUMN seller_id BIGINT;
ALTER TABLE daily_settlement ADD CONSTRAINT fk_daily_settlement_seller
    FOREIGN KEY (seller_id) REFERENCES sellers (id);

-- 정산번호 (UI의 정산ID)
ALTER TABLE daily_settlement ADD COLUMN settlement_number VARCHAR(50);

-- 공제/환급 상세 내역
ALTER TABLE daily_settlement ADD COLUMN delivery_fee_change DECIMAL(15,2);
ALTER TABLE daily_settlement ADD COLUMN balance_offset DECIMAL(15,2);

-- 판매자별 날짜 범위 조회 최적화 인덱스
CREATE INDEX idx_daily_settlement_seller_scheduled ON daily_settlement (seller_id, scheduled_date);
