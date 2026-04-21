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

-- 3. board_statistic 삽입 (EntityGraph 로딩 대응)
INSERT INTO board_statistic (board_id, basic_score, board_wish_count, board_review_count,
                             board_view_count, board_review_grade, is_deleted, created_at, modified_at)
SELECT id, 0.0, 0, 0, 0, 0.0, 0, NOW(), NOW()
FROM product_board
WHERE title LIKE '[MOCK]%'
  AND sale_status = 'PENDING'
  AND is_deleted = 0;
