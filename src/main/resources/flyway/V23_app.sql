-- type 크기 25 -> 100 조정
-- type + seller_id를 유니크로 조정
ALTER TABLE seller_documents
    MODIFY COLUMN type VARCHAR(100),
    ADD CONSTRAINT uq_seller_documents_seller_id_type UNIQUE (seller_id, type);
