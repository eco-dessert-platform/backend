-- create_cart_tables.sql

CREATE TABLE cart (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_cart_member UNIQUE (member_id),
    CONSTRAINT fk_cart_member FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE cart_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    request VARCHAR(200) NULL,
    created_at DATETIME(6) NOT NULL,
    modified_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_cart_item UNIQUE (cart_id, item_id),
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart (id),
    CONSTRAINT fk_cart_item_board FOREIGN KEY (item_id) REFERENCES product_board (id)
);

CREATE TABLE cart_option (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cart_item_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    modified_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_cart_option UNIQUE (cart_item_id, option_id),
    CONSTRAINT fk_cart_option_cart_item FOREIGN KEY (cart_item_id) REFERENCES cart_item (id),
    CONSTRAINT fk_cart_option_product FOREIGN KEY (option_id) REFERENCES product (id)
);

INSERT INTO cart (member_id)
SELECT m.id
FROM member m
WHERE NOT EXISTS (
    SELECT 1
    FROM cart c
    WHERE c.member_id = m.id
);