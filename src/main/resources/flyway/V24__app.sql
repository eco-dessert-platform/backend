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

-- Add admin_id column to refresh_token table
ALTER TABLE refresh_token
    ADD COLUMN admin_id BIGINT UNIQUE;

-- member id null 값 허용
ALTER TABLE refresh_token
    MODIFY member_id BIGINT NULL;

ALTER TABLE member
DROP COLUMN member_role;