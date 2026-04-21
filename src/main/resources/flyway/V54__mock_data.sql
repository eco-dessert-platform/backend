-- FE API 테스트용 Mock 데이터
-- [MOCK] 접두사로 식별 및 삭제 용이

-- 1. Mock Store 5개 삽입
INSERT INTO store (name, introduce, profile, is_deleted, created_at, modified_at)
VALUES
    ('[MOCK] 밀담 베이커리', '건강한 저당 빵을 만드는 베이커리', NULL, 0, NOW(), NOW()),
    ('[MOCK] 그린웨이브', '비건 전문 베이커리', NULL, 0, NOW(), NOW()),
    ('[MOCK] 오트하우스', '귀리 전문 베이커리', NULL, 0, NOW(), NOW()),
    ('[MOCK] 헬시브레드', '단백질 강화 베이커리', NULL, 0, NOW(), NOW()),
    ('[MOCK] 케토팩토리', '키토제닉 전문 베이커리', NULL, 0, NOW(), NOW());

-- 2. PENDING 상태 product_board 20개 삽입 (스토어당 4개)
INSERT INTO product_board (store_id, title, price, sale_status, is_deleted, created_at, modified_at)
SELECT s.id, pb.title, pb.price, 'PENDING', 0, NOW(), NOW()
FROM store s
         JOIN (
    SELECT '[MOCK] 밀담 베이커리' AS store_name, '[MOCK] 저당 쌀빵 세트'          AS title, 28000 AS price
    UNION ALL SELECT '[MOCK] 밀담 베이커리', '[MOCK] 글루텐프리 식빵', 32000
    UNION ALL SELECT '[MOCK] 밀담 베이커리', '[MOCK] 유기농 통밀 바게트', 18000
    UNION ALL SELECT '[MOCK] 밀담 베이커리', '[MOCK] 두부 크림 케이크', 45000
    UNION ALL SELECT '[MOCK] 그린웨이브', '[MOCK] 비건 초코 머핀 4종 세트', 22000
    UNION ALL SELECT '[MOCK] 그린웨이브', '[MOCK] 아마씨 비건 식빵', 19000
    UNION ALL SELECT '[MOCK] 그린웨이브', '[MOCK] 코코넛 오일 스콘 세트', 26000
    UNION ALL SELECT '[MOCK] 그린웨이브', '[MOCK] 두유 파운드케이크', 38000
    UNION ALL SELECT '[MOCK] 오트하우스', '[MOCK] 귀리 그래놀라 쿠키', 15000
    UNION ALL SELECT '[MOCK] 오트하우스', '[MOCK] 오트밀 바나나 머핀', 24000
    UNION ALL SELECT '[MOCK] 오트하우스', '[MOCK] 통귀리 식빵', 21000
    UNION ALL SELECT '[MOCK] 오트하우스', '[MOCK] 귀리 베이글 6종 세트', 34000
    UNION ALL SELECT '[MOCK] 헬시브레드', '[MOCK] 단백질 강화 스콘 세트', 29000
    UNION ALL SELECT '[MOCK] 헬시브레드', '[MOCK] 닭가슴살 프로틴 식빵', 35000
    UNION ALL SELECT '[MOCK] 헬시브레드', '[MOCK] 고단백 아몬드 크래커', 16000
    UNION ALL SELECT '[MOCK] 헬시브레드', '[MOCK] 퀴노아 에너지 바 세트', 42000
    UNION ALL SELECT '[MOCK] 케토팩토리', '[MOCK] 키토 아몬드 식빵', 38000
    UNION ALL SELECT '[MOCK] 케토팩토리', '[MOCK] 저탄 베이글 세트', 32000
    UNION ALL SELECT '[MOCK] 케토팩토리', '[MOCK] 치즈 크래커 박스', 25000
    UNION ALL SELECT '[MOCK] 케토팩토리', '[MOCK] 코코넛 카카오 파운드', 41000
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
INSERT INTO product_board (store_id, title, price, sale_status, is_crawling, is_deleted, created_at, modified_at)
SELECT s.id, pb.title, pb.price, 'ON_SALE', 1, 0, NOW(), NOW()
FROM store s
         JOIN (
    SELECT '[MOCK] 밀담 베이커리' AS store_name, '[MOCK] 크로와상 세트'      AS title, 28000 AS price
    UNION ALL SELECT '[MOCK] 밀담 베이커리', '[MOCK] 호밀 샌드위치 빵', 24000
    UNION ALL SELECT '[MOCK] 그린웨이브', '[MOCK] 비건 크로와상', 22000
    UNION ALL SELECT '[MOCK] 그린웨이브', '[MOCK] 플랜트 케이크', 48000
    UNION ALL SELECT '[MOCK] 오트하우스', '[MOCK] 오트밀 쿠키 박스', 18000
    UNION ALL SELECT '[MOCK] 오트하우스', '[MOCK] 귀리 타르트 세트', 32000
    UNION ALL SELECT '[MOCK] 헬시브레드', '[MOCK] 프로틴 식빵', 35000
    UNION ALL SELECT '[MOCK] 헬시브레드', '[MOCK] 아몬드 프로틴 바', 26000
    UNION ALL SELECT '[MOCK] 케토팩토리', '[MOCK] 키토 크래커', 19000
    UNION ALL SELECT '[MOCK] 케토팩토리', '[MOCK] 저탄 케이크', 52000
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
    -- [MOCK] 크로와상 세트 옵션
    SELECT '[MOCK] 크로와상 세트'    AS board_title, '[MOCK] 밀담 베이커리' AS store_name,
           '[MOCK] 2개입'           AS title, 0     AS price, 80  AS stock, 'BREAD' AS category,
           1 AS gluten_free_tag, 0 AS high_protein_tag, 1 AS sugar_free_tag, 0 AS vegan_tag, 0 AS ketogenic_tag, 0 AS low_fat_tag,
           1 AS monday, 0 AS tuesday, 1 AS wednesday, 0 AS thursday, 1 AS friday, 0 AS saturday, 0 AS sunday
    UNION ALL
    SELECT '[MOCK] 크로와상 세트', '[MOCK] 밀담 베이커리',
           '[MOCK] 4개입', 8000, 50, 'BREAD',
           1, 0, 1, 0, 0, 0,
           1, 0, 1, 0, 1, 0, 0
    -- [MOCK] 호밀 샌드위치 빵 옵션
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
    -- [MOCK] 비건 크로와상 옵션
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
    -- [MOCK] 플랜트 케이크 옵션
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
    -- [MOCK] 오트밀 쿠키 박스 옵션
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
    -- [MOCK] 귀리 타르트 세트 옵션
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
    -- [MOCK] 프로틴 식빵 옵션
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
    -- [MOCK] 아몬드 프로틴 바 옵션
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
    -- [MOCK] 키토 크래커 옵션
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
    -- [MOCK] 저탄 케이크 옵션
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
-- getUpdateStoreNames: status = 'PENDING' 인 store_name_request
-- =====================================================================

-- 7. store_name_request 10개 삽입 (스토어당 2개, PENDING)
INSERT INTO store_name_request (store_id, seller_id, current_name, new_name, status, created_at)
SELECT s.id, NULL, s.name, r.new_name, 'PENDING', NOW()
FROM store s
         JOIN (
    SELECT '[MOCK] 밀담 베이커리' AS store_name, '[MOCK] 밀담브레드' AS new_name
    UNION ALL SELECT '[MOCK] 밀담 베이커리', '[MOCK] 밀담베이커스'
    UNION ALL SELECT '[MOCK] 그린웨이브', '[MOCK] 그린웨이브 비건'
    UNION ALL SELECT '[MOCK] 그린웨이브', '[MOCK] 그린플랜트베이커리'
    UNION ALL SELECT '[MOCK] 오트하우스', '[MOCK] 오트하우스 키친'
    UNION ALL SELECT '[MOCK] 오트하우스', '[MOCK] 오트웰니스'
    UNION ALL SELECT '[MOCK] 헬시브레드', '[MOCK] 헬시브레드랩'
    UNION ALL SELECT '[MOCK] 헬시브레드', '[MOCK] 프로틴베이커리'
    UNION ALL SELECT '[MOCK] 케토팩토리', '[MOCK] 케토팩토리 프리미엄'
    UNION ALL SELECT '[MOCK] 케토팩토리', '[MOCK] 키토브레드'
) r ON s.name = r.store_name;
