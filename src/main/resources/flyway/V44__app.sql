DROP INDEX IF EXISTS idx_store_name_request_status_created_at ON store_name_request;
CREATE INDEX idx_store_name_request_status_created_at_id ON store_name_request(status, created_at ASC, id ASC);
