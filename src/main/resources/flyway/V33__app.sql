-- 1. 판매자 사유 컬럼 추가
ALTER TABLE return_request
    ADD COLUMN seller_comment VARCHAR(255) COMMENT '판매자의 승인/거절 사유';

-- 2. order_item_history 테이블 변경
-- 기존 PK를 드롭한 후, 타입 변경 + AUTO_INCREMENT + PK 설정을 한 번에 처리합니다.
ALTER TABLE order_item_history DROP PRIMARY KEY;

ALTER TABLE order_item_history
    MODIFY COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY;