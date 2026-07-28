-- FE API 테스트용 Mock 데이터
-- [MOCK] 접두사로 식별 및 삭제 용이

-- 1. Mock Store 5개 삽입
--    identifier varchar(16) NOT NULL, name varchar(255) NOT NULL UNIQUE
INSERT INTO store (identifier, name, introduce, profile, is_deleted, created_at, modified_at)
VALUES
    ('mock-mildam',     '[MOCK] 밀담 베이커리', '건강한 저당 빵을 만드는 베이커리', NULL, 0, NOW(), NOW()),
    ('mock-greenwave',  '[MOCK] 그린웨이브',    '비건 전문 베이커리',              NULL, 0, NOW(), NOW()),
    ('mock-oathouse',   '[MOCK] 오트하우스',    '귀리 전문 베이커리',              NULL, 0, NOW(), NOW()),
    ('mock-healthbrd',  '[MOCK] 헬시브레드',    '단백질 강화 베이커리',            NULL, 0, NOW(), NOW()),
    ('mock-ketofactory','[MOCK] 케토팩토리',    '키토제닉 전문 베이커리',          NULL, 0, NOW(), NOW());

-- =====================================================================
-- getUploadApprovals: sale_status = 'PENDING' 인 product_board
-- =====================================================================

-- 2. PENDING 상태 product_board 20개 삽입 (스토어당 4개)
--    is_soldout tinyint NOT NULL (default 없음) → 명시 필요
INSERT INTO product_board (store_id, title, price, sale_status, is_soldout, is_deleted, created_at, modified_at)
SELECT s.id, pb.title, pb.price, 'PENDING', 0, 0, NOW(), NOW()
FROM store s
         JOIN (
    SELECT '[MOCK] 밀담 베이커리' AS store_name, '[MOCK] 저당 쌀빵 세트'     AS title, 28000 AS price
    UNION ALL SELECT '[MOCK] 밀담 베이커리', '[MOCK] 글루텐프리 식빵',        32000
    UNION ALL SELECT '[MOCK] 밀담 베이커리', '[MOCK] 유기농 통밀 바게트',     18000
    UNION ALL SELECT '[MOCK] 밀담 베이커리', '[MOCK] 두부 크림 케이크',       45000
    UNION ALL SELECT '[MOCK] 그린웨이브',    '[MOCK] 비건 초코 머핀 4종 세트',22000
    UNION ALL SELECT '[MOCK] 그린웨이브',    '[MOCK] 아마씨 비건 식빵',       19000
    UNION ALL SELECT '[MOCK] 그린웨이브',    '[MOCK] 코코넛 오일 스콘 세트',  26000
    UNION ALL SELECT '[MOCK] 그린웨이브',    '[MOCK] 두유 파운드케이크',      38000
    UNION ALL SELECT '[MOCK] 오트하우스',    '[MOCK] 귀리 그래놀라 쿠키',     15000
    UNION ALL SELECT '[MOCK] 오트하우스',    '[MOCK] 오트밀 바나나 머핀',     24000
    UNION ALL SELECT '[MOCK] 오트하우스',    '[MOCK] 통귀리 식빵',            21000
    UNION ALL SELECT '[MOCK] 오트하우스',    '[MOCK] 귀리 베이글 6종 세트',   34000
    UNION ALL SELECT '[MOCK] 헬시브레드',    '[MOCK] 단백질 강화 스콘 세트',  29000
    UNION ALL SELECT '[MOCK] 헬시브레드',    '[MOCK] 닭가슴살 프로틴 식빵',   35000
    UNION ALL SELECT '[MOCK] 헬시브레드',    '[MOCK] 고단백 아몬드 크래커',   16000
    UNION ALL SELECT '[MOCK] 헬시브레드',    '[MOCK] 퀴노아 에너지 바 세트',  42000
    UNION ALL SELECT '[MOCK] 케토팩토리',    '[MOCK] 키토 아몬드 식빵',       38000
    UNION ALL SELECT '[MOCK] 케토팩토리',    '[MOCK] 저탄 베이글 세트',       32000
    UNION ALL SELECT '[MOCK] 케토팩토리',    '[MOCK] 치즈 크래커 박스',       25000
    UNION ALL SELECT '[MOCK] 케토팩토리',    '[MOCK] 코코넛 카카오 파운드',   41000
) pb ON s.name = pb.store_name;

-- 3. board_statistic 삽입 - getUploadApprovals EntityGraph 로딩 대응
INSERT INTO board_statistic (board_id, basic_score, board_wish_count, board_review_count,
                             board_view_count, board_review_grade, is_deleted, created_at, modified_at)
SELECT id, 0.0, 0, 0, 0, 0.0, 0, NOW(), NOW()
FROM product_board
WHERE title LIKE '[MOCK]%'
  AND sale_status = 'PENDING'
  AND is_deleted = 0;

-- =====================================================================
-- getAdminBoards: is_crawling = 1 인 product_board + product 옵션
-- =====================================================================

-- 4. is_crawling = 1 인 product_board 10개 삽입 (스토어당 2개, ON_SALE)
--    is_soldout tinyint NOT NULL (default 없음) → 명시 필요
INSERT INTO product_board (store_id, title, price, sale_status, is_crawling, is_soldout, is_deleted, created_at, modified_at)
SELECT s.id, pb.title, pb.price, 'ON_SALE', 1, 0, 0, NOW(), NOW()
FROM store s
         JOIN (
    SELECT '[MOCK] 밀담 베이커리' AS store_name, '[MOCK] 크로와상 세트'   AS title, 28000 AS price
    UNION ALL SELECT '[MOCK] 밀담 베이커리', '[MOCK] 호밀 샌드위치 빵',   24000
    UNION ALL SELECT '[MOCK] 그린웨이브',    '[MOCK] 비건 크로와상',      22000
    UNION ALL SELECT '[MOCK] 그린웨이브',    '[MOCK] 플랜트 케이크',      48000
    UNION ALL SELECT '[MOCK] 오트하우스',    '[MOCK] 오트밀 쿠키 박스',   18000
    UNION ALL SELECT '[MOCK] 오트하우스',    '[MOCK] 귀리 타르트 세트',   32000
    UNION ALL SELECT '[MOCK] 헬시브레드',    '[MOCK] 프로틴 식빵',        35000
    UNION ALL SELECT '[MOCK] 헬시브레드',    '[MOCK] 아몬드 프로틴 바',   26000
    UNION ALL SELECT '[MOCK] 케토팩토리',    '[MOCK] 키토 크래커',        19000
    UNION ALL SELECT '[MOCK] 케토팩토리',    '[MOCK] 저탄 케이크',        52000
) pb ON s.name = pb.store_name;

-- 5. product 옵션 삽입 (board당 2~3개, 다양한 태그 조합)
INSERT INTO product (product_board_id, store_id, title, price, stock, category,
                     gluten_free_tag, high_protein_tag, sugar_free_tag, vegan_tag, ketogenic_tag, low_fat_tag,
                     monday, tuesday, wednesday, thursday, friday, saturday, sunday,
                     is_soldout, is_deleted, created_at, modified_at)
SELECT pb.id, pb.store_id, p.title, p.price, p.stock, p.category,
       p.gluten_free_tag, p.high_protein_tag, p.sugar_free_tag, p.vegan_tag, p.ketogenic_tag, p.low_fat_tag,
       p.monday, p.tuesday, p.wednesday, p.thursday, p.friday, p.saturday, p.sunday,
       0, 0, NOW(), NOW()
FROM product_board pb
         JOIN (
    SELECT '[MOCK] 크로와상 세트'   AS board_title, '[MOCK] 밀담 베이커리' AS store_name,
           '[MOCK] 2개입'          AS title, 0     AS price, 80  AS stock, 'BREAD' AS category,
           1 AS gluten_free_tag, 0 AS high_protein_tag, 1 AS sugar_free_tag, 0 AS vegan_tag, 0 AS ketogenic_tag, 0 AS low_fat_tag,
           1 AS monday, 0 AS tuesday, 1 AS wednesday, 0 AS thursday, 1 AS friday, 0 AS saturday, 0 AS sunday
    UNION ALL
    SELECT '[MOCK] 크로와상 세트', '[MOCK] 밀담 베이커리',
           '[MOCK] 4개입', 8000, 50, 'BREAD',
           1, 0, 1, 0, 0, 0,
           1, 0, 1, 0, 1, 0, 0
    UNION ALL
    SELECT '[MOCK] 호밀 샌드위치 빵', '[MOCK] 밀담 베이커리',
           '[MOCK] 1개', 0, 60, 'BREAD',
           1, 0, 0, 0, 0, 1,
           0, 1, 0, 1, 0, 1, 0
    UNION ALL
    SELECT '[MOCK] 호밀 샌드위치 빵', '[MOCK] 밀담 베이커리',
           '[MOCK] 2개 세트', 5000, 40, 'BREAD',
           1, 0, 0, 0, 0, 1,
           0, 1, 0, 1, 0, 1, 0
    UNION ALL
    SELECT '[MOCK] 비건 크로와상', '[MOCK] 그린웨이브',
           '[MOCK] 3개입', 0, 70, 'BREAD',
           1, 0, 1, 1, 0, 1,
           1, 0, 0, 1, 0, 1, 0
    UNION ALL
    SELECT '[MOCK] 비건 크로와상', '[MOCK] 그린웨이브',
           '[MOCK] 6개입', 9000, 35, 'BREAD',
           1, 0, 1, 1, 0, 1,
           1, 0, 0, 1, 0, 1, 0
    UNION ALL
    SELECT '[MOCK] 플랜트 케이크', '[MOCK] 그린웨이브',
           '[MOCK] 미니 1호', 0, 20, 'CAKE',
           0, 0, 1, 1, 0, 0,
           0, 0, 1, 0, 0, 1, 0
    UNION ALL
    SELECT '[MOCK] 플랜트 케이크', '[MOCK] 그린웨이브',
           '[MOCK] 2호', 12000, 15, 'CAKE',
           0, 0, 1, 1, 0, 0,
           0, 0, 1, 0, 0, 1, 0
    UNION ALL
    SELECT '[MOCK] 오트밀 쿠키 박스', '[MOCK] 오트하우스',
           '[MOCK] 10개입', 0, 100, 'COOKIE',
           1, 0, 1, 0, 0, 1,
           0, 1, 0, 0, 1, 0, 0
    UNION ALL
    SELECT '[MOCK] 오트밀 쿠키 박스', '[MOCK] 오트하우스',
           '[MOCK] 20개입', 7000, 60, 'COOKIE',
           1, 0, 1, 0, 0, 1,
           0, 1, 0, 0, 1, 0, 0
    UNION ALL
    SELECT '[MOCK] 귀리 타르트 세트', '[MOCK] 오트하우스',
           '[MOCK] 4개입', 0, 45, 'TART',
           1, 0, 0, 0, 0, 1,
           1, 0, 1, 0, 1, 0, 0
    UNION ALL
    SELECT '[MOCK] 귀리 타르트 세트', '[MOCK] 오트하우스',
           '[MOCK] 8개입', 10000, 30, 'TART',
           1, 0, 0, 0, 0, 1,
           1, 0, 1, 0, 1, 0, 0
    UNION ALL
    SELECT '[MOCK] 프로틴 식빵', '[MOCK] 헬시브레드',
           '[MOCK] 1개', 0, 55, 'BREAD',
           0, 1, 1, 0, 0, 1,
           0, 1, 0, 1, 0, 0, 0
    UNION ALL
    SELECT '[MOCK] 프로틴 식빵', '[MOCK] 헬시브레드',
           '[MOCK] 2개 묶음', 8000, 30, 'BREAD',
           0, 1, 1, 0, 0, 1,
           0, 1, 0, 1, 0, 0, 0
    UNION ALL
    SELECT '[MOCK] 프로틴 식빵', '[MOCK] 헬시브레드',
           '[MOCK] 3개 묶음', 14000, 20, 'BREAD',
           0, 1, 1, 0, 0, 1,
           0, 1, 0, 1, 0, 0, 0
    UNION ALL
    SELECT '[MOCK] 아몬드 프로틴 바', '[MOCK] 헬시브레드',
           '[MOCK] 5개입', 0, 90, 'COOKIE',
           0, 1, 1, 0, 0, 0,
           1, 1, 1, 1, 1, 0, 0
    UNION ALL
    SELECT '[MOCK] 아몬드 프로틴 바', '[MOCK] 헬시브레드',
           '[MOCK] 10개입', 9000, 50, 'COOKIE',
           0, 1, 1, 0, 0, 0,
           1, 1, 1, 1, 1, 0, 0
    UNION ALL
    SELECT '[MOCK] 키토 크래커', '[MOCK] 케토팩토리',
           '[MOCK] 1봉', 0, 75, 'COOKIE',
           1, 0, 1, 0, 1, 1,
           0, 1, 0, 1, 0, 0, 0
    UNION ALL
    SELECT '[MOCK] 키토 크래커', '[MOCK] 케토팩토리',
           '[MOCK] 3봉 세트', 6000, 40, 'COOKIE',
           1, 0, 1, 0, 1, 1,
           0, 1, 0, 1, 0, 0, 0
    UNION ALL
    SELECT '[MOCK] 저탄 케이크', '[MOCK] 케토팩토리',
           '[MOCK] 1호', 0, 25, 'CAKE',
           0, 0, 1, 0, 1, 0,
           0, 0, 1, 0, 0, 1, 0
    UNION ALL
    SELECT '[MOCK] 저탄 케이크', '[MOCK] 케토팩토리',
           '[MOCK] 2호', 15000, 15, 'CAKE',
           0, 0, 1, 0, 1, 0,
           0, 0, 1, 0, 0, 1, 0
) p ON pb.title = p.board_title
    AND pb.store_id = (SELECT id FROM store WHERE name = p.store_name)
    AND pb.is_crawling = 1;

-- 6. board_statistic 삽입 - getAdminBoards EntityGraph 로딩 대응
INSERT INTO board_statistic (board_id, basic_score, board_wish_count, board_review_count,
                             board_view_count, board_review_grade, is_deleted, created_at, modified_at)
SELECT id, 0.0, 0, 0, 0, 0.0, 0, NOW(), NOW()
FROM product_board
WHERE title LIKE '[MOCK]%'
  AND is_crawling = 1
  AND is_deleted = 0;

-- =====================================================================
-- getSellerApplicationList: store_application (PENDING) + sellers + account_verifications
-- getUpdateStoreNames: store_name_request (PENDING)
-- 공통 전제: sellers에 store_id 연결 (store_name_request.seller_id NOT NULL 충족)
-- =====================================================================

-- 7. Mock sellers 5개 삽입 (store_id 포함 → store_name_request FK 충족)
--    uk_sellers_store_id UNIQUE, uk_seller_provider_id UNIQUE
INSERT INTO sellers (name, provider, provider_id, status, store_id, is_deleted, created_at)
SELECT av.name, av.provider, av.provider_id, 'PENDING', s.id, 0, NOW()
FROM store s
         JOIN (
    SELECT '[MOCK] 밀담 베이커리' AS store_name, '[MOCK]김민준' AS name, 'KAKAO'  AS provider, 'MOCK_SELLER_001' AS provider_id
    UNION ALL SELECT '[MOCK] 그린웨이브',         '[MOCK]이서연',  'KAKAO',         'MOCK_SELLER_002'
    UNION ALL SELECT '[MOCK] 오트하우스',          '[MOCK]박지호',  'GOOGLE',        'MOCK_SELLER_003'
    UNION ALL SELECT '[MOCK] 헬시브레드',          '[MOCK]최유나',  'GOOGLE',        'MOCK_SELLER_004'
    UNION ALL SELECT '[MOCK] 케토팩토리',          '[MOCK]정하은',  'KAKAO',         'MOCK_SELLER_005'
) av ON s.name = av.store_name;

-- 8. account_verifications 5개 삽입 (verified=1, account_number=NULL → decrypt 시 null 반환)
INSERT INTO account_verifications (seller_id, bank_code, account_number, account_holder, verified, created_at, modified_at)
SELECT s.id, av.bank_code, NULL, av.account_holder, 1, NOW(), NOW()
FROM sellers s
         JOIN (
    SELECT 'MOCK_SELLER_001' AS provider_id, '004' AS bank_code, '[MOCK]김민준' AS account_holder
    UNION ALL SELECT 'MOCK_SELLER_002', '020', '[MOCK]이서연'
    UNION ALL SELECT 'MOCK_SELLER_003', '088', '[MOCK]박지호'
    UNION ALL SELECT 'MOCK_SELLER_004', '081', '[MOCK]최유나'
    UNION ALL SELECT 'MOCK_SELLER_005', '011', '[MOCK]정하은'
) av ON s.provider_id = av.provider_id;

-- 9. store_application 5개 삽입 (status=PENDING)
INSERT INTO store_application (seller_id, store_id, name, introduce, profile, status,
                               phone, sub_phone, email,
                               origin_address_line, origin_address_detail,
                               created_at, modified_at)
SELECT s.id, NULL, sa.name, sa.introduce, NULL, 'PENDING',
       sa.phone, NULL, sa.email,
       sa.origin_address_line, sa.origin_address_detail,
       NOW(), NOW()
FROM sellers s
         JOIN (
    SELECT 'MOCK_SELLER_001' AS provider_id,
           '[MOCK] 밀담 베이커리'              AS name,
           '건강한 저당 빵 전문'               AS introduce,
           '01012341001'                     AS phone,
           'mock1@example.com'              AS email,
           '(13494) 경기도 성남시 분당구 판교역로 235' AS origin_address_line,
           '판교 테크원타워 1동 101호'         AS origin_address_detail
    UNION ALL
    SELECT 'MOCK_SELLER_002', '[MOCK] 그린웨이브', '비건 전문 베이커리', '01012341002', 'mock2@example.com',
           '(04524) 서울시 중구 세종대로 110', '광화문빌딩 3층 302호'
    UNION ALL
    SELECT 'MOCK_SELLER_003', '[MOCK] 오트하우스', '귀리 전문 베이커리', '01012341003', 'mock3@example.com',
           '(06236) 서울시 강남구 테헤란로 152', '강남파이낸스센터 2층 205호'
    UNION ALL
    SELECT 'MOCK_SELLER_004', '[MOCK] 헬시브레드', '단백질 강화 베이커리', '01012341004', 'mock4@example.com',
           '(48058) 부산시 해운대구 센텀중앙로 55', '센텀시티타워 7층 701호'
    UNION ALL
    SELECT 'MOCK_SELLER_005', '[MOCK] 케토팩토리', '키토제닉 전문 베이커리', '01012341005', 'mock5@example.com',
           '(61452) 광주시 동구 금남로 245', '광주금남빌딩 4층 401호'
) sa ON s.provider_id = sa.provider_id;

-- =====================================================================
-- getUpdateStoreNames: store_name_request (PENDING)
-- =====================================================================

-- 10. store_name_request 10개 삽입 (스토어당 2개)
--     seller_id NOT NULL, store_id NOT NULL → sellers.store_id 로 JOIN
INSERT INTO store_name_request (store_id, seller_id, current_name, new_name, status, created_at)
SELECT s.id, sel.id, s.name, r.new_name, 'PENDING', NOW()
FROM store s
         JOIN sellers sel ON sel.store_id = s.id AND sel.provider_id LIKE 'MOCK_SELLER_%'
         JOIN (
    SELECT '[MOCK] 밀담 베이커리' AS store_name, '[MOCK] 밀담브레드'         AS new_name
    UNION ALL SELECT '[MOCK] 밀담 베이커리', '[MOCK] 밀담베이커스'
    UNION ALL SELECT '[MOCK] 그린웨이브',    '[MOCK] 그린웨이브 비건'
    UNION ALL SELECT '[MOCK] 그린웨이브',    '[MOCK] 그린플랜트베이커리'
    UNION ALL SELECT '[MOCK] 오트하우스',    '[MOCK] 오트하우스 키친'
    UNION ALL SELECT '[MOCK] 오트하우스',    '[MOCK] 오트웰니스'
    UNION ALL SELECT '[MOCK] 헬시브레드',    '[MOCK] 헬시브레드랩'
    UNION ALL SELECT '[MOCK] 헬시브레드',    '[MOCK] 프로틴베이커리'
    UNION ALL SELECT '[MOCK] 케토팩토리',    '[MOCK] 케토팩토리 프리미엄'
    UNION ALL SELECT '[MOCK] 케토팩토리',    '[MOCK] 키토브레드'
) r ON s.name = r.store_name;
