-- ================================================
-- Add UNIQUE to name + Add status column (MariaDB)
-- ================================================

-- 1) name 컬럼을 NOT NULL로 변경 (기본값 필요 없음)
ALTER TABLE store
    MODIFY name VARCHAR(255) NOT NULL;

-- 2) name 컬럼에 UNIQUE 제약 추가
ALTER TABLE store
    ADD CONSTRAINT uk_store_name UNIQUE (name);

-- 3) status 컬럼 추가 (enum 타입이 아니라 VARCHAR)
ALTER TABLE store
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'NONE';
