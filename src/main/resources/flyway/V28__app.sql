-- Description: Create seller table
CREATE TABLE seller (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        name VARCHAR(15) NOT NULL,
                        provider VARCHAR(20) NOT NULL,
                        provider_id VARCHAR(50) NOT NULL UNIQUE,
                        status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                        is_deleted TINYINT(1) DEFAULT 0,
                        store_id BIGINT,
                        created_at DATETIME(6) NOT NULL,
                        PRIMARY KEY (id),
                        CONSTRAINT fk_seller_store FOREIGN KEY (store_id)
                            REFERENCES store(id)
);