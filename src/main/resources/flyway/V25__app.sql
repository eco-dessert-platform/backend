-- Add admin_id column to refresh_token table
ALTER TABLE refresh_token
    ADD COLUMN admin_id BIGINT UNIQUE;
