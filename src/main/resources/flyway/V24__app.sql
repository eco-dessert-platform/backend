-- Description: Create admin table
CREATE TABLE admin (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       account_id VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       name VARCHAR(255) NOT NULL,
                       created_at DATETIME(6) NOT NULL,
                       modified_at DATETIME(6) ,
                       PRIMARY KEY (id)
);

ALTER TABLE bakery.refresh_token
    -- 1) 기존 단일 유니크 제약 삭제
    DROP INDEX member_id,

    -- 2) member_id → user_id 로 컬럼명 변경
    CHANGE COLUMN member_id user_id BIGINT NOT NULL,

    -- 3) user_role 컬럼 추가
    ADD COLUMN user_role VARCHAR(50) NOT NULL AFTER user_id,

    -- 4) 복합 유니크 인덱스 생성
    ADD UNIQUE KEY uk_user_id_user_role (user_id, user_role);

ALTER TABLE member
DROP COLUMN member_role;