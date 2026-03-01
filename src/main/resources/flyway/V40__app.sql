ALTER TABLE exchange_request
    ADD COLUMN seller_comment VARCHAR(255) COMMENT '판매자의 승인/거절 사유';