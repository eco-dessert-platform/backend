-- =========================================================
-- 주문 생성 · 결제 승인 도입
--   · payment 1 : orders N 으로 전환 (멀티 스토어 결제 지원)
--   · 주문자 이메일 / 주문번호 UNIQUE 추가
-- 기존 운영 데이터가 있다고 가정하고 백필을 포함한다. (데이터가 없어도 그대로 통과)
-- =========================================================

-- ---------------------------------------------------------
-- 1) orders.payment_id 추가 + 기존 payment.order_id 역이관
-- ---------------------------------------------------------
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS payment_id BIGINT NULL AFTER seller_id;

UPDATE orders o
    JOIN payment p ON p.order_id = o.id
SET o.payment_id = p.id;

-- ---------------------------------------------------------
-- 2) payment 신규 컬럼 (백필을 위해 우선 NULL 허용으로 추가)
-- ---------------------------------------------------------
ALTER TABLE payment
    ADD COLUMN IF NOT EXISTS payment_number VARCHAR(64)  NULL,
    ADD COLUMN IF NOT EXISTS member_id      BIGINT       NULL,
    ADD COLUMN IF NOT EXISTS total_amount   INT          NULL,
    ADD COLUMN IF NOT EXISTS payment_key    VARCHAR(200) NULL;

-- 기존 결제: 연결된 주문에서 회원/금액을 가져오고 결제번호를 생성한다.
UPDATE payment p
    JOIN orders o ON o.payment_id = p.id
SET p.payment_number = CONCAT('PAY-LEGACY-', LPAD(p.id, 10, '0')),
    p.member_id      = o.member_id,
    p.total_amount   = o.total_amount;

-- 주문과 연결되지 않은 고아 결제도 번호를 채운다. (NOT NULL 제약 대비)
UPDATE payment
SET payment_number = CONCAT('PAY-LEGACY-', LPAD(id, 10, '0'))
WHERE payment_number IS NULL;

-- ---------------------------------------------------------
-- 3) 제약 적용
--    payment_key 는 UNIQUE + NULL 허용. MariaDB 는 NULL 을 중복으로 보지 않으므로
--    결제 전(PENDING) 행이 여러 개여도 충돌하지 않는다.
-- ---------------------------------------------------------
ALTER TABLE payment
    MODIFY COLUMN payment_number VARCHAR(64) NOT NULL;

ALTER TABLE payment
    ADD CONSTRAINT uk_payment_number UNIQUE (payment_number),
    ADD CONSTRAINT uk_payment_key    UNIQUE (payment_key);

CREATE INDEX idx_payment_member ON payment (member_id);

-- ---------------------------------------------------------
-- 4) payment.order_id 제거 (1:1 제약 해제)
-- ---------------------------------------------------------
ALTER TABLE payment DROP FOREIGN KEY IF EXISTS fk_payment_orders;
ALTER TABLE payment DROP COLUMN IF EXISTS order_id;

-- ---------------------------------------------------------
-- 5) orders FK / 인덱스
-- ---------------------------------------------------------
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_payment FOREIGN KEY (payment_id) REFERENCES payment (id);

CREATE INDEX idx_orders_payment ON orders (payment_id);

-- ---------------------------------------------------------
-- 6) 주문자 이메일
-- ---------------------------------------------------------
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS buyer_email VARCHAR(100) NULL AFTER buyer_sub_phone;

-- ---------------------------------------------------------
-- 7) 주문번호 백필 후 UNIQUE
-- ---------------------------------------------------------
UPDATE orders
SET order_number = CONCAT('ORDER-LEGACY-', LPAD(id, 10, '0'))
WHERE order_number IS NULL OR order_number = '';

ALTER TABLE orders
    ADD CONSTRAINT uk_orders_order_number UNIQUE (order_number);
