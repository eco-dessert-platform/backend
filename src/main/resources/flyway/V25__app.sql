ALTER TABLE notice
ADD COLUMN links JSON NULL,
ADD COLUMN image_links JSON NULL,
ADD COLUMN modified_at datetime(6) NULL,
ADD COLUMN admin_id INT NULL;

