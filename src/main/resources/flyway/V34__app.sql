-- store 테이블에 컬럼 추가
ALTER TABLE store
    ADD COLUMN IF NOT EXISTS phone varchar(20) NULL,
    ADD COLUMN IF NOT EXISTS sub_phone varchar(20) NULL,
    ADD COLUMN IF NOT EXISTS email varchar(100) NULL,
    ADD COLUMN IF NOT EXISTS origin_address_line varchar(255) NULL,
    ADD COLUMN IF NOT EXISTS origin_address_detail varchar(255) NULL;

-- sellers → store 데이터 이관 (sellers 비어있으면 그냥 0 rows)
UPDATE store s
    JOIN sellers se ON se.store_id = s.id
    SET
        s.phone = se.phone,
        s.sub_phone = se.sub_phone,
        s.email = se.email,
        s.origin_address_line = se.origin_address_line,
        s.origin_address_detail = se.origin_address_detail,
        s.profile = COALESCE(s.profile, se.profile);

-- sellers 테이블에서 이동된 컬럼 제거
ALTER TABLE sellers
    DROP COLUMN IF EXISTS phone,
    DROP COLUMN IF EXISTS sub_phone,
    DROP COLUMN IF EXISTS email,
    DROP COLUMN IF EXISTS origin_address_line,
    DROP COLUMN IF EXISTS origin_address_detail,
    DROP COLUMN IF EXISTS profile;

-- member_id FK 동적 제거 (환경 의존 제거)
SET @fk_name = (
    SELECT constraint_name
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE table_schema = DATABASE()
      AND table_name = 'sellers'
      AND column_name = 'member_id'
      AND referenced_table_name IS NOT NULL
    LIMIT 1
);

SET @sql = IF(
    @fk_name IS NOT NULL,
    CONCAT('ALTER TABLE sellers DROP FOREIGN KEY ', @fk_name),
    'SELECT 1'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- member_id 컬럼 / 인덱스 제거
ALTER TABLE sellers DROP COLUMN IF EXISTS member_id;
DROP INDEX IF EXISTS member_id ON sellers;

-- sellers → seller 구조로 변경 (sellers 데이터 0건 전제)
ALTER TABLE sellers
    ADD COLUMN IF NOT EXISTS name varchar(15) NOT NULL DEFAULT 'UNKNOWN',
    ADD COLUMN IF NOT EXISTS provider varchar(20) NOT NULL,
    ADD COLUMN IF NOT EXISTS provider_id varchar(50) NOT NULL,
    ADD COLUMN IF NOT EXISTS status varchar(20) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS is_deleted tinyint(1) NULL DEFAULT 0,
    DROP COLUMN IF EXISTS modified_at;

-- provider_id UNIQUE 제약 추가 (중복 방지)
ALTER TABLE sellers
    ADD CONSTRAINT uk_seller_provider_id UNIQUE (provider_id);

-- 기존 임시 seller 테이블 제거
DROP TABLE IF EXISTS seller;
