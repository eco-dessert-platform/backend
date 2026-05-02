ALTER TABLE seller_documents
    ADD COLUMN name VARCHAR(60) NOT NULL;

ALTER TABLE seller_documents
    MODIFY COLUMN type VARCHAR(60);
