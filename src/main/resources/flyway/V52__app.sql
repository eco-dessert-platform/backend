-- 분석테이블 인덱스 추가
CREATE INDEX idx_seller_stat_date ON seller_statistics_daily (seller_id, stat_date);