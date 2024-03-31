CREATE SCHEMA IF NOT EXISTS bakery;

use bakery;

create user 'bbangle'@'%' identified by '9893';
grant all privileges on bakery.* to 'bbangle'@'%';

CREATE TABLE member
(
    id          BIGINT AUTO_INCREMENT,
    email       VARCHAR(255),
    phone       VARCHAR(11),
    name        VARCHAR(15),
    nickname    VARCHAR(20),
    birth       VARCHAR(10),
    profile     VARCHAR(255),
    provider    varchar(20),
    provider_id varchar(50),
    created_at  DATETIME(6),
    modified_at DATETIME(6),
    is_deleted  TINYINT,
    PRIMARY KEY (id)
);

CREATE TABLE signature_agreement
(
    id                BIGINT AUTO_INCREMENT,
    member_id         BIGINT       NOT NULL,
    name              VARCHAR(100) NOT NULL,
    date_of_signature DATETIME(6)  NOT NULL,
    agreement_status  TINYINT      NOT NULL DEFAULT 1,
    CONSTRAINT signature_agreement_pk PRIMARY KEY (id),
    CONSTRAINT fk_member FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE search
(
    id         BIGINT AUTO_INCREMENT,
    member_id  BIGINT,
    user_type  VARCHAR(20),
    keyword    VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    CONSTRAINT search_pk PRIMARY KEY (id),
    CONSTRAINT fk_member_search FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE store
(
    id          BIGINT AUTO_INCREMENT,
    identifier  VARCHAR(16)  NOT NULL,
    name        VARCHAR(255) NOT NULL,
    introduce   VARCHAR(255),
    profile     VARCHAR(255),
    created_at  DATETIME(6),
    modified_at DATETIME(6),
    is_deleted  TINYINT,
    CONSTRAINT store_pk PRIMARY KEY (id)
);

CREATE TABLE product_board
(
    id           BIGINT AUTO_INCREMENT,
    store_id     BIGINT       NOT NULL,
    title        VARCHAR(50)  NOT NULL,
    price        INT          NOT NULL,
    status       TINYINT      NOT NULL,
    profile      VARCHAR(255),
    detail       VARCHAR(255) NOT NULL,
    purchase_url VARCHAR(255) NOT NULL,
    view         INT          NOT NULL DEFAULT 0,
    sunday       TINYINT      NOT NULL DEFAULT 0,
    monday       TINYINT      NOT NULL DEFAULT 0,
    tuesday      TINYINT      NOT NULL DEFAULT 0,
    wednesday    TINYINT      NOT NULL DEFAULT 0,
    thursday     TINYINT      NOT NULL DEFAULT 0,
    friday       TINYINT      NOT NULL DEFAULT 0,
    saturday     TINYINT      NOT NULL DEFAULT 0,
    created_at   DATETIME(6),
    modified_at  DATETIME(6),
    is_deleted   TINYINT,
    wish_cnt     INT,
    CONSTRAINT product_board_pk PRIMARY KEY (id),
    CONSTRAINT fk_store_product_board FOREIGN KEY (store_id) REFERENCES store (id)
);

CREATE TABLE wishlist_folder
(
    id          BIGINT AUTO_INCREMENT,
    member_id   BIGINT NOT NULL,
    folder_name VARCHAR(50),
    created_at  DATETIME(6),
    modified_at DATETIME(6),
    is_deleted  TINYINT,
    CONSTRAINT wishlist_folder_pk PRIMARY KEY (id),
    CONSTRAINT fk_member_wishlist_folder FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE wishlist_product
(
    id                 BIGINT AUTO_INCREMENT,
    wishlist_folder_id BIGINT NOT NULL,
    product_board_id   BIGINT NOT NULL,
    created_at         DATETIME(6),
    modified_at        DATETIME(6),
    member_id          BIGINT,
    is_deleted         TINYINT,
    CONSTRAINT wishlist_product_pk PRIMARY KEY (id),
    CONSTRAINT fk_wishlist_folder_wishlist_product FOREIGN KEY (wishlist_folder_id) REFERENCES wishlist_folder (id),
    CONSTRAINT fk_product_board_wishlist_product FOREIGN KEY (product_board_id) REFERENCES product_board (id)
);

CREATE TABLE wishlist_store
(
    id          BIGINT AUTO_INCREMENT,
    member_id   BIGINT NOT NULL,
    store_id    BIGINT NOT NULL,
    created_at  DATETIME(6),
    modified_at DATETIME(6),
    is_deleted  TINYINT,
    CONSTRAINT wishlist_store_pk PRIMARY KEY (id),
    CONSTRAINT fk_member_wishlist_store FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_store_wishlist_store FOREIGN KEY (store_id) REFERENCES store (id)
);

CREATE TABLE product
(
    id               BIGINT AUTO_INCREMENT,
    product_board_id BIGINT      NOT NULL,
    title            VARCHAR(50) NOT NULL,
    price            INT,
    category         VARCHAR(20) NOT NULL,
    gluten_free_tag  TINYINT     NOT NULL DEFAULT 0,
    high_protein_tag TINYINT     NOT NULL DEFAULT 0,
    sugar_free_tag   TINYINT     NOT NULL DEFAULT 0,
    vegan_tag        TINYINT              DEFAULT 0,
    ketogenic_tag    TINYINT     NOT NULL DEFAULT 0,
    CONSTRAINT product_pk PRIMARY KEY (id),
    CONSTRAINT fk_product_board_product FOREIGN KEY (product_board_id) REFERENCES product_board (id)
);

CREATE TABLE product_img
(
    id               BIGINT AUTO_INCREMENT,
    product_board_id BIGINT       NOT NULL,
    url              VARCHAR(255) NOT NULL,
    CONSTRAINT product_img_pk PRIMARY KEY (id),
    CONSTRAINT fk_product_board_product_img FOREIGN KEY (product_board_id) REFERENCES product_board (id)
);

CREATE TABLE refresh_token
(
    id               BIGINT AUTO_INCREMENT,
    member_id        BIGINT       NOT NULL,
    refresh_token    VARCHAR(255) NOT NULL,
    CONSTRAINT refresh_token_pk PRIMARY KEY (id),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (member_id) REFERENCES member (id)
);
