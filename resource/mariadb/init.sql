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
    is_deleted tinyint,
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

create table product_board
(
    id                       bigint auto_increment primary key,
    store_id                 bigint        not null,
    title                    varchar(50)   not null,
    price                    int           not null,
    is_soldout               tinyint       not null,
    profile                  varchar(255)  null,
    purchase_url             varchar(255)  not null,
    view                     int default 0 not null,
    created_at               datetime(6)   null,
    modified_at              datetime(6)   null,
    is_deleted               tinyint       null,
    wish_cnt                 int           null,
    delivery_fee             int           null,
    free_shipping_conditions int           null,
    constraint fk_store_product_board
        foreign key (store_id) references store (id)
) charset = utf8mb4;


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

create table product
(
    id               bigint auto_increment primary key,
    product_board_id bigint            not null,
    title            varchar(50)       not null,
    price            int               null,
    category         varchar(20)       not null,
    gluten_free_tag  tinyint default 0 not null,
    high_protein_tag tinyint default 0 not null,
    sugar_free_tag   tinyint default 0 not null,
    vegan_tag        tinyint default 0 null,
    ketogenic_tag    tinyint default 0 not null,
    monday           tinyint default 0 not null,
    tuesday          tinyint default 0 not null,
    wednesday        tinyint default 0 not null,
    thursday         tinyint default 0 not null,
    friday           tinyint default 0 not null,
    saturday         tinyint default 0 not null,
    sunday           tinyint default 0 not null,
    order_start_date datetime          null,
    order_end_date   datetime          null,
    sugars           int               null,
    protein          int               null,
    carbohydrates    int               null,
    fat              int               null,
    weight           int               null,
    calories         int               null,
    is_soldout       tinyint default 0 not null,        null,
    constraint fk_product_board_product
        foreign key (product_board_id) references product_board (id)
) charset = utf8mb4;

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
    id            BIGINT AUTO_INCREMENT,
    member_id     BIGINT       NOT NULL,
    refresh_token VARCHAR(255) NOT NULL,
    CONSTRAINT refresh_token_pk PRIMARY KEY (id),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE boardStatistic
(
    id                 BIGINT AUTO_INCREMENT,
    board_id           BIGINT  NOT NULL,
    basic_score        DOUBLE  NOT NULL default 0,
    board_wish_count   INT     NOT NULL default 0,
    board_review_count INT     NOT NULL default 0,
    board_view_count   INT     NOT NULL default 0,
    board_view_grade   DECIMAL NOT NULL default 0,
    CONSTRAINT product_img_pk PRIMARY KEY (id)
);

CREATE TABLE notice
(
    id         BIGINT AUTO_INCREMENT,
    title      varchar(255) NOT NULL,
    content    varchar(255) NOT NULL,
    CONSTRAINT product_img_pk PRIMARY KEY (id),
    created_at DATETIME(6)
);

create table product_detail
(
    id               bigint auto_increment,
    product_board_id bigint,
    img_index        int,
    url              varchar(255),
    CONSTRAINT product_detail_pk PRIMARY KEY (id),
    constraint fk_product_board_product_detail
        foreign key (product_board_id) references product_board (id)
);

create table withdrawal
(
    id          bigint auto_increment,
    member_id   bigint       not null,
    reason      varchar(255) null,
    created_at  datetime(6)  null,
    modified_at datetime(6)  null,
    CONSTRAINT withdrawal_pk PRIMARY KEY (id),
    constraint fk_member_withdrawal
        foreign key (member_id) references member (id)
);

CREATE TABLE push
(
    id            BIGINT AUTO_INCREMENT,
    fcm_token     VARCHAR(255) NOT NULL,
    member_id     BIGINT       NOT NULL,
    product_id    BIGINT       NOT NULL,
    push_category VARCHAR(255) NOT NULL,
    is_subscribed TINYINT      NOT NULL,
    created_at    DATETIME(6)  NULL,
    modified_at   DATETIME(6)  NULL,
    CONSTRAINT push_pk PRIMARY KEY (id),
    INDEX idx_member_product_id (member_id, product_id)
);