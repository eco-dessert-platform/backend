-- create_cart_tables.sql

CREATE TABLE cart (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    request VARCHAR(200) NULL,
    created_at DATETIME(6) NOT NULL,
    modified_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_cart_member_item UNIQUE (member_id, item_id),
    CONSTRAINT fk_cart_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT fk_cart_item FOREIGN KEY (item_id) REFERENCES product_board(id)
);

CREATE TABLE cart_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    modified_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_cart_item_option UNIQUE (cart_id, option_id),
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart(id),
    CONSTRAINT fk_cart_item_option FOREIGN KEY (option_id) REFERENCES product(id)
);