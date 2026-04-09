ALTER TABLE product_board
    ADD COLUMN rejection_category VARCHAR(100) NULL COMMENT '거절 사유 카테고리',
    ADD COLUMN rejection_reason VARCHAR(500) NULL COMMENT '거절 상세 사유',
    ADD COLUMN rejection_at DATETIME NULL COMMENT '거절 시간';
