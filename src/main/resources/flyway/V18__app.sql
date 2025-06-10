CREATE TABLE product_info_notice
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name            VARCHAR(50) NOT NULL, -- 제품명
    food_type               VARCHAR(255),         -- 식품 유형
    manufacturer            VARCHAR(255),         -- 제조사 / 생산자
    origin_location         VARCHAR(255),         -- 원산지 / 제조소 소재지
    manufacture_date        VARCHAR(255),         -- 제조년월일
    expiration_date         VARCHAR(255),         -- 소비기한 또는 품질 유지기한
    storage_guide           VARCHAR(255),         -- 보관 방법 및 주의사항
    packaging_quantity_unit VARCHAR(255),         -- 포장 단위별 용량(중량) 및 수량
    raw_material_name       VARCHAR(255),         -- 원재료명 및 함량
    nutrition_info          VARCHAR(255),         -- 영양 정보
    transgenic              VARCHAR(255),         -- 유전자 변형 식품 여부
    customer_warning        VARCHAR(255),         -- 소비자 안전 정보 및 주의사항
    import_food             VARCHAR(255)          -- 수입 식품 관련 정보
);

