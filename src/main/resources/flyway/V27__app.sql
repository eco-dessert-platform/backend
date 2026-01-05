--- 1. 컬럼 추가
ALTER TABLE product
    ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0,
    ADD COLUMN created_at DATETIME(6) NOT NULL,
    ADD COLUMN modified_at DATETIME(6) NOT NULL;

ALTER TABLE product_img
    ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0,
    ADD COLUMN created_at DATETIME(6) NOT NULL,
    ADD COLUMN modified_at DATETIME(6) NOT NULL;

ALTER TABLE product_detail
    ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0,
    ADD COLUMN created_at DATETIME(6) NOT NULL,
    ADD COLUMN modified_at DATETIME(6) NOT NULL;

ALTER TABLE board_statistic
    ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0,
    ADD COLUMN created_at DATETIME(6) NOT NULL,
    ADD COLUMN modified_at DATETIME(6) NOT NULL;

ALTER TABLE product_info_notice
    ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0;

-- 2. 기본값 세팅
UPDATE product
SET created_at = NOW(),
    modified_at = NOW();

UPDATE product_img
SET created_at = NOW(),
    modified_at = NOW();

UPDATE product_detail
SET created_at = NOW(),
    modified_at = NOW();

UPDATE board_statistic
SET created_at = NOW(),
    modified_at = NOW();

-- 3. board 기준 삭제 상태 동기화
UPDATE product p
    JOIN product_board b ON p.board_id = b.id
    SET p.is_deleted = 1
WHERE b.is_deleted = 1;

UPDATE product_img pi
    JOIN product_board b ON pi.board_id = b.id
    SET pi.is_deleted = 1
WHERE b.is_deleted = 1;

UPDATE product_detail pd
    JOIN product_board b ON pd.board_id = b.id
    SET pd.is_deleted = 1
WHERE b.is_deleted = 1;

UPDATE board_statistic bs
    JOIN product_board b ON bs.board_id = b.id
    SET bs.is_deleted = 1
WHERE b.is_deleted = 1;

UPDATE board_statistic bs
    JOIN product_board b ON bs.board_id = b.id
    SET bs.is_deleted = 1
WHERE b.is_deleted = 1;

UPDATE product_info_notice pin
    JOIN product_board b ON pin.id = b.product_info_notice_id
    SET pin.is_deleted = 1
WHERE b.is_deleted = 1;

