ALTER TABLE notice
DROP COLUMN links,
    ADD COLUMN is_delete TINYINT(1) DEFAULT 0 NOT NULL COMMENT '삭제 여부 (0: 미삭제, 1: 삭제)';