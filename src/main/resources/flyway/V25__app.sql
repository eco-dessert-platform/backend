-- Add admin_id column to refresh_token table
ALTER TABLE refresh_token
    ADD COLUMN admin_id BIGINT UNIQUE;

-- member id null 값 허용
ALTER TABLE refresh_token
    MODIFY member_id BIGINT NULL;


