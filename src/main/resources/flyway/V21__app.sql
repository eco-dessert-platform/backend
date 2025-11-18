-- ----------------------------
-- 현준님
-- ----------------------------
ALTER TABLE product_board
    ADD COLUMN courier VARCHAR(50);

-- ----------------------------
-- 준규님
-- ----------------------------
CREATE TABLE sellers
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone                 VARCHAR(20),
    sub_phone             VARCHAR(20),
    email                 VARCHAR(100),
    origin_address_line   VARCHAR(255),
    origin_address_detail VARCHAR(255),
    profile               VARCHAR(255),
    status                VARCHAR(20),
    member_id             BIGINT,
    store_id              BIGINT,
    created_at            datetime(6) NOT NULL,
    modified_at           datetime(6) NOT NULL,
    is_deleted            TINYINT(1) DEFAULT 0,
    INDEX (member_id),
    INDEX (store_id)
);

CREATE TABLE account_verifications
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_name      VARCHAR(50),
    account_number VARBINARY(255),
    account_holder VARCHAR(50),
    verified       TINYINT(1),
    seller_id      BIGINT,
    created_at     datetime(6) NOT NULL,
    modified_at    datetime(6) NOT NULL,
    is_deleted     TINYINT(1) DEFAULT 0,
    INDEX (seller_id)
);

CREATE TABLE seller_documents
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id   BIGINT,
    type        VARCHAR(30),
    url         VARCHAR(255),
    status      VARCHAR(30),
    created_at  datetime(6) NOT NULL,
    modified_at datetime(6) NOT NULL,
    is_deleted  TINYINT(1) DEFAULT 0,
    INDEX (seller_id)
);

CREATE TABLE orders
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number    VARCHAR(30),
    order_date      datetime(6),
    buyer_name      VARCHAR(20),
    buyer_phone     VARCHAR(20),
    buyer_sub_phone VARCHAR(20),
    delivery_fee    INT,
    total_amount    INT,
    member_id       BIGINT,
    created_at      datetime(6) NOT NULL,
    modified_at     datetime(6) NOT NULL,
    is_deleted      TINYINT(1) DEFAULT 0,
    INDEX (member_id)
);

CREATE TABLE order_item
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity        INT,
    product_price   INT,
    unit_price      INT,
    order_status    VARCHAR(20),
    delivery_status VARCHAR(20),
    total_price     INT,
    order_id        BIGINT,
    product_id      BIGINT,
    created_at      datetime(6) NOT NULL,
    modified_at     datetime(6) NOT NULL,
    is_deleted      TINYINT(1) DEFAULT 0,
    INDEX (order_id),
    INDEX (product_id)
);

CREATE TABLE order_delivery
(
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_item_id            BIGINT,
    sender_name              VARCHAR(20),  -- 발신자 이름
    sender_phone             VARCHAR(20),  -- 발신자 연락처
    sender_address           VARCHAR(200), -- 발신자 주소
    sender_address_detail    VARCHAR(200), -- 발신자 상세 주소
    sender_zip_code          VARCHAR(10),
    recipient_name           VARCHAR(20),  -- 수신자 이름
    recipient_phone1         VARCHAR(20),  -- 수신자 연락처1
    recipient_phone2         VARCHAR(20),  -- 수신자 연락처2
    recipient_address        VARCHAR(200), -- 수신자 주소
    recipient_address_detail VARCHAR(200), -- 수신자 상세 주소
    recipient_zip_code       VARCHAR(10),
    delivery_memo            VARCHAR(255), -- 배송 메모
    courier_name             VARCHAR(50),  -- 택배사명
    tracking_number          VARCHAR(50),  -- 송장번호
    fee                      INT,          -- 배송비
    status                   VARCHAR(30),  -- 배송 상태 (예: REQUESTED, SHIPPED, DELIVERED 등)
    shipped_at               DATETIME(6),  -- 출고 일시
    delivered_at             DATETIME(6),
    created_at               datetime(6) NOT NULL,
    modified_at              datetime(6) NOT NULL,
    INDEX (order_item_id)
);

CREATE TABLE order_item_history
(
    id            VARCHAR(255) PRIMARY KEY,
    order_status  VARCHAR(20),
    order_item_id BIGINT,
    created_at    datetime(6) NOT NULL,
    INDEX (order_item_id)
);

ALTER TABLE account_verifications
    ADD CONSTRAINT fk_account_verifications_seller FOREIGN KEY (seller_id) REFERENCES sellers (id);

ALTER TABLE seller_documents
    ADD CONSTRAINT fk_seller_documents_seller FOREIGN KEY (seller_id) REFERENCES sellers (id);

ALTER TABLE order_item
    ADD CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (id);

ALTER TABLE order_delivery
    ADD CONSTRAINT fk_order_delivery_order_item FOREIGN KEY (order_item_id) REFERENCES order_item (id);

ALTER TABLE order_item_history
    ADD CONSTRAINT fk_order_item_history_order_item FOREIGN KEY (order_item_id) REFERENCES order_item (id);

-- ----------------------------
-- 경민님
-- ----------------------------
CREATE TABLE claim
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_type    VARCHAR(31) NOT NULL, -- 상속 구분 (CANCEL / RETURN / EXCHANGE)
    order_item_id BIGINT,               -- 주문 상품 FK
    detail_reason VARCHAR(255),         -- 상세 사유
    decided_at    DATETIME(6),          -- 처리 결정 일시
    created_at    DATETIME(6),          -- 생성 일시
    modified_at   DATETIME(6),          -- 수정 일시
    CONSTRAINT fk_claim_order_item FOREIGN KEY (order_item_id) REFERENCES order_item (id)
);

CREATE TABLE cancel_request
(
    id     BIGINT NOT NULL PRIMARY KEY,
    status VARCHAR(30), -- 요청 상태 (REQUESTED / APPROVED / REJECTED / COMPLETED)
    CONSTRAINT fk_cancel_request_claim FOREIGN KEY (id) REFERENCES claim (id)
);

CREATE TABLE return_request
(
    id     BIGINT NOT NULL PRIMARY KEY,
    status VARCHAR(30), -- 요청 상태 (REQUESTED / PICKUP_SCHEDULED / ...)
    CONSTRAINT fk_return_request_claim FOREIGN KEY (id) REFERENCES claim (id)
);

CREATE TABLE exchange_request
(
    id     BIGINT NOT NULL PRIMARY KEY,
    status VARCHAR(30), -- 요청 상태 (REQUESTED / PICKUP_SCHEDULED / ...)
    CONSTRAINT fk_exchange_request_claim FOREIGN KEY (id) REFERENCES claim (id)
);

CREATE TABLE claim_image
(
    id        BIGINT NOT NULL AUTO_INCREMENT,
    claim_id  BIGINT,
    url       VARCHAR(255),
    img_order INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_claim_image_claim FOREIGN KEY (claim_id) REFERENCES claim (id)
);

CREATE TABLE claim_delivery
(
    id                       BIGINT NOT NULL AUTO_INCREMENT, -- PK
    claim_id                 BIGINT,                         -- 클레임 ID (FK: claim.id)

    -- 배송 유형 (출고, 반품 수거, 교환 수거, 교환 재배송 등)
    delivery_type            VARCHAR(30),

    -- 발신자 정보
    sender_name              VARCHAR(20),                    -- 발신자 이름
    sender_phone             VARCHAR(20),                    -- 발신자 연락처
    sender_address           VARCHAR(200),                   -- 발신자 주소
    sender_address_detail    VARCHAR(200),                   -- 발신자 상세 주소
    sender_zip_code          VARCHAR(10),                    -- 발신자 우편번호

    -- 수신자 정보
    recipient_name           VARCHAR(20),                    -- 수신자 이름
    recipient_phone1         VARCHAR(20),                    -- 수신자 연락처1
    recipient_phone2         VARCHAR(20),                    -- 수신자 연락처2
    recipient_address        VARCHAR(200),                   -- 수신자 주소
    recipient_address_detail VARCHAR(200),                   -- 수신자 상세 주소
    recipient_zip_code       VARCHAR(10),                    -- 수신자 우편번호

    -- 배송 상세
    delivery_memo            VARCHAR(255),                   -- 배송 메모
    courier_name             VARCHAR(50),                    -- 택배사명
    tracking_number          VARCHAR(50),                    -- 송장번호
    fee                      INT,                            -- 배송비
    status                   VARCHAR(30),                    -- 배송 상태 (예: REQUESTED, SHIPPED, DELIVERED 등)
    shipped_at               DATETIME(6),                    -- 출고 일시
    collected_at             DATETIME(6),                    -- 수거 완료 일시
    delivered_at             DATETIME(6),                    -- 배송 완료 일시

    -- 공통 관리 필드
    created_at               DATETIME(6),                    -- 생성 일시
    modified_at              DATETIME(6),                    -- 수정 일시

    PRIMARY KEY (id),
    CONSTRAINT fk_claim_delivery_claim FOREIGN KEY (claim_id) REFERENCES claim (id)
);

CREATE TABLE payment
(
    id             BIGINT NOT NULL AUTO_INCREMENT,                               -- PK
    order_id       BIGINT,                                                       -- 주문 ID (FK)
    payment_status VARCHAR(50),                                                  -- 결제 상태 (예: PENDING, COMPLETED 등)
    payment_method VARCHAR(50),                                                  -- 결제 수단 (예: CARD, KAKAO_PAY 등)
    paid_at        DATETIME(6),
    created_at     DATETIME(6),                                                  -- 생성 일시
    modified_at    DATETIME(6),                                                  -- 수정 일시
    PRIMARY KEY (id),
    CONSTRAINT fk_payment_orders FOREIGN KEY (order_id) REFERENCES `orders` (id) -- 주문 테이블 참조
);