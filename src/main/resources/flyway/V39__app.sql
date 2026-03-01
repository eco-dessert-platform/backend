-- product_board 테이블에 sale_status 컬럼 추가
ALTER TABLE product_board ADD COLUMN sale_status VARCHAR(20) NULL;

-- 기존 데이터 전체 ON_SALE로 일괄 마이그레이션
UPDATE product_board SET sale_status = 'ON_SALE';

-- NOT NULL 제약 추가
ALTER TABLE product_board MODIFY COLUMN sale_status VARCHAR(20) NOT NULL;
