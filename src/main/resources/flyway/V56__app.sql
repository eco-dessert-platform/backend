-- 인스타그램 추적 링크 시드 데이터 (channel만 등록; 실제 destination URL은 yml의 @Value로 런타임 주입)
INSERT INTO tracking_link (channel, description, created_at, modified_at)
VALUES ('INSTAGRAM', '인스타 바이오 링크', NOW(6), NOW(6));
