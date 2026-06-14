CREATE TABLE member_delivery_address (
    id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id       BIGINT       NOT NULL,
    address_name    VARCHAR(50)  NOT NULL,
    is_default      TINYINT(1)   NOT NULL DEFAULT 0,
    recipient_name  VARCHAR(100) NOT NULL,
    phone           VARCHAR(20)  NOT NULL,
    address         VARCHAR(255) NOT NULL,
    address_detail  VARCHAR(255),
    zip_code        VARCHAR(10),
    is_deleted      TINYINT(1)   NOT NULL DEFAULT 0,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    modified_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_mda_member FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE INDEX idx_mda_member_id ON member_delivery_address (member_id);
