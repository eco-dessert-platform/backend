CREATE TABLE productInfoNotice (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
productName VARCHAR(50) NOT NULL,         -- 제품명
foodType VARCHAR(255),                    -- 식품 유형
manufacturer VARCHAR(255),                -- 제조사 / 생산자
originLocation VARCHAR(255),              -- 원산지 / 제조소 소재지
manufactureDate VARCHAR(255),             -- 제조년월일
expirationDate VARCHAR(255),              -- 소비기한 또는 품질 유지기한
storageGuide VARCHAR(255),                -- 보관 방법 및 주의사항
packagingQuantityUnit VARCHAR(255),       -- 포장 단위별 용량(중량) 및 수량
rawMaterialName VARCHAR(255),             -- 원재료명 및 함량
nutritionInfo VARCHAR(255),               -- 영양 정보
transgenic VARCHAR(255),                  -- 유전자 변형 식품 여부
customerWarning VARCHAR(255),             -- 소비자 안전 정보 및 주의사항
importFood VARCHAR(255)                   -- 수입 식품 관련 정보
);
ALTER TABLE product ADD COLUMN stock INT NOT NULL DEFAULT 0;
ALTER TABLE product_board ADD COLUMN product_info_notice_id BIGINT;

