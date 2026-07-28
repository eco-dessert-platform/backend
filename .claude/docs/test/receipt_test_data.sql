-- 영수증 조회 API 테스트용 데이터
-- GET /api/v1/customer/orders/{orderId}/receipt
--
-- 실행 전 확인사항:
--   1. V59 마이그레이션 적용 여부 확인 (payment 테이블에 approval_number 컬럼 존재하는지)
--      → SELECT column_name FROM information_schema.columns
--           WHERE table_name = 'payment' AND column_name = 'approval_number';
--
-- 삭제 시 역순:
--   DELETE FROM payment      WHERE order_id = (SELECT id FROM orders WHERE order_number = '20100024120');
--   DELETE FROM order_item   WHERE order_id = (SELECT id FROM orders WHERE order_number = '20100024120');
--   DELETE FROM orders       WHERE order_number = '20100024120';
--   DELETE FROM product      WHERE product_board_id = (SELECT id FROM product_board WHERE title = '비건빵 3종 모음');
--   DELETE FROM product_board WHERE title = '비건빵 3종 모음';
--   DELETE FROM sellers      WHERE provider_id = 'kakao_seller_001';
--   DELETE FROM store        WHERE identifier = '521-03-12345';
--   DELETE FROM member       WHERE provider_id = 'kakao_test_001';

-- =====================================================================
-- 1. member
--    phone varchar(11) → 하이픈 없이 11자리
-- =====================================================================
INSERT INTO member (email, phone, name, nickname, birth, profile, provider, provider_id, is_deleted, created_at, modified_at)
VALUES ('test@bbangle.com', '01012345678', '테스트유저', '빵순이', '19950101', NULL, 'KAKAO', 'kakao_test_001', 0, NOW(), NOW());

-- =====================================================================
-- 2. store
--    identifier varchar(16) → 한국 사업자번호 형식 XXX-XX-XXXXX (12자)
--    created_at/modified_at: default current_timestamp() 있으므로 생략 가능
-- =====================================================================
INSERT INTO store (identifier, name, introduce, is_deleted)
VALUES ('521-03-12345', '빵그리의 오븐', '건강한 베이커리', 0);

-- =====================================================================
-- 3. sellers
--    modified_at 컬럼 없음 (SoftDeleteCreatedAtBaseEntity 사용)
--    name varchar(15) 제한
-- =====================================================================
INSERT INTO sellers (name, provider, provider_id, status, store_id, created_at)
VALUES ('판매자1', 'KAKAO', 'kakao_seller_001', 'APPROVED',
        (SELECT id FROM store WHERE identifier = '521-03-12345'),
        NOW());

-- =====================================================================
-- 4. product_board
--    is_soldout tinyint NOT NULL (default 없음 → 명시 필요)
-- =====================================================================
INSERT INTO product_board (store_id, title, price, sale_status, is_soldout, is_deleted, created_at, modified_at)
VALUES (
    (SELECT id FROM store WHERE identifier = '521-03-12345'),
    '비건빵 3종 모음', 11100, 'ON_SALE', 0, 0, NOW(), NOW()
);

-- =====================================================================
-- 5. product (3개)
--    store_id NOT NULL → product_board의 store_id 사용
-- =====================================================================
INSERT INTO product (product_board_id, store_id, title, price, stock, category,
                     gluten_free_tag, high_protein_tag, sugar_free_tag, vegan_tag, ketogenic_tag, low_fat_tag,
                     monday, tuesday, wednesday, thursday, friday, saturday, sunday,
                     is_soldout, is_deleted, created_at, modified_at)
SELECT pb.id, pb.store_id, p.title, 3700, 100, 'BREAD',
       1, 0, 1, 1, 0, 0,
       1, 1, 1, 1, 1, 0, 0,
       0, 0, NOW(), NOW()
FROM product_board pb
         JOIN (SELECT '비건 쌀빵 1종'  AS title
               UNION ALL SELECT '비건 현미빵 1종'
               UNION ALL SELECT '비건 귀리빵 1종') p ON 1=1
WHERE pb.title = '비건빵 3종 모음' AND pb.is_deleted = 0;

-- =====================================================================
-- 6. orders
--    buyer_phone varchar(20) → 하이픈 없이 저장
-- =====================================================================
INSERT INTO orders (order_number, order_date, buyer_name, buyer_phone,
                    delivery_fee, total_amount, member_id, seller_id,
                    created_at, modified_at)
VALUES ('20100024120', '2025-05-29 18:00:10', '테스트유저', '01012345678',
        0, 11100,
        (SELECT id FROM member  WHERE provider_id = 'kakao_test_001'),
        (SELECT id FROM sellers WHERE provider_id = 'kakao_seller_001'),
        NOW(), NOW());

-- =====================================================================
-- 7. order_item (3개: 정상 2 + 취소 1)
--    delivery_status: OrderDeliveryStatus enum 값 사용
-- =====================================================================
INSERT INTO order_item (quantity, product_price, unit_price, order_status, delivery_status,
                        total_price, order_id, product_id, created_at, modified_at)
SELECT 1, 3700, 3700, oi.order_status, 'PAYMENT_COMPLETE', 3700,
       (SELECT id FROM orders WHERE order_number = '20100024120'),
       p.id,
       NOW(), NOW()
FROM product p
         JOIN product_board pb ON p.product_board_id = pb.id
         JOIN (SELECT '비건 쌀빵 1종'  AS title, 'PURCHASE_CONFIRMED' AS order_status
               UNION ALL SELECT '비건 현미빵 1종', 'PURCHASE_CONFIRMED'
               UNION ALL SELECT '비건 귀리빵 1종', 'CANCEL_APPROVED') oi ON p.title = oi.title
WHERE pb.title = '비건빵 3종 모음' AND pb.is_deleted = 0;

-- =====================================================================
-- 8. payment (V59 적용 후 실행)
--    V59 미적용 시 approval_number 이하 4개 컬럼 제거하고 실행
-- =====================================================================
INSERT INTO payment (order_id, payment_status, payment_method, paid_at,
                     approval_number, card_type, card_number, installment,
                     created_at, modified_at)
VALUES (
    (SELECT id FROM orders WHERE order_number = '20100024120'),
    'COMPLETED', 'CARD', '2025-05-29 18:00:10',
    '1232241511', 'SHINHAN', '4843221034561234', '일시불',
    NOW(), NOW()
);

-- =====================================================================
-- 확인 쿼리
-- =====================================================================
-- 주문·결제 기본 정보
SELECT o.id AS order_id, o.order_number, m.name AS buyer,
       s.name AS store_name, st.identifier AS business_number,
       o.total_amount, p.payment_method, p.paid_at, p.approval_number
FROM orders o
         JOIN member  m  ON o.member_id  = m.id
         JOIN sellers sl ON o.seller_id  = sl.id
         JOIN store   st ON sl.store_id  = st.id
         JOIN store   s  ON sl.store_id  = s.id
         LEFT JOIN payment p ON p.order_id = o.id
WHERE o.order_number = '20100024120';

-- 주문 항목 (취소 항목 확인)
SELECT oi.order_status, pr.title, oi.total_price
FROM order_item oi
         JOIN product  pr ON oi.product_id = pr.id
         JOIN orders   o  ON oi.order_id   = o.id
WHERE o.order_number = '20100024120';
