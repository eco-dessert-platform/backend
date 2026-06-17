-- create_cart_tables.sql

CREATE TABLE cart (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    board_id BIGINT NOT NULL,
    request VARCHAR(200) NULL,
    created_at DATETIME(6) NOT NULL,
    modified_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_cart_member_board UNIQUE (member_id, board_id),
    CONSTRAINT fk_cart_member FOREIGN KEY (member_id) REFERENCES member(id),
    CONSTRAINT fk_cart_board FOREIGN KEY (board_id) REFERENCES product_board(id)
);

CREATE TABLE cart_option (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cart_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    modified_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_cart_option_option UNIQUE (cart_id, option_id),
    CONSTRAINT fk_cart_option_cart FOREIGN KEY (cart_id) REFERENCES cart(id),
    CONSTRAINT fk_cart_option_option FOREIGN KEY (option_id) REFERENCES product(id)
);