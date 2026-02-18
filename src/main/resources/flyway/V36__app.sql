-- product_board 테이블에 누락된 컬럼 추가
ALTER TABLE product_board
    ADD COLUMN IF NOT EXISTS discount_type VARCHAR(20),
    ADD COLUMN IF NOT EXISTS discount_value INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS delivery_condition VARCHAR(20),
    ADD COLUMN IF NOT EXISTS is_fresh TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS production_start_time VARCHAR(20);

-- 기존 discount_rate 데이터를 discount_value로 마이그레이션 (0 포함 전체 행 처리)
UPDATE product_board
SET discount_value = discount_rate,
    discount_type = 'RATE'
WHERE discount_rate >= 0;

-- product 테이블에 누락된 컬럼 추가
ALTER TABLE product
    ADD COLUMN IF NOT EXISTS low_fat_tag TINYINT(1) NOT NULL DEFAULT 0;

-- product_board 테이블의 purchase_url nullable로 변경
ALTER TABLE product_board
    MODIFY COLUMN purchase_url VARCHAR(255) NULL;

-- product_info_notice 테이블에 누락된 컬럼 추가
ALTER TABLE product_info_notice
    ADD COLUMN IF NOT EXISTS created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN IF NOT EXISTS modified_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    ADD COLUMN IF NOT EXISTS is_deleted TINYINT(1) NOT NULL DEFAULT 0;
