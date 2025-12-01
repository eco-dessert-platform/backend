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
