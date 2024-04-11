CREATE SCHEMA IF NOT EXISTS bakery;

use bakery;

CREATE USER IF NOT EXISTS 'bbangle'@'%' IDENTIFIED BY '9893';
GRANT ALL PRIVILEGES ON bakery.* TO 'bbangle'@'%';

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


create table signature_agreement (
    id                bigint auto_increment primary key,
    member_id         bigint            not null,
    name              varchar(100)      not null,
    date_of_signature datetime(6)       not null,
    agreement_status  tinyint default 1 not null,
    constraint fk_member foreign key (member_id) references member (id)
);

create table search (
    id         bigint auto_increment primary key,
    member_id  bigint            null,
    is_deleted tinyint default 0 not null,
    keyword    varchar(255)      not null,
    created_at datetime(6)       null,
    constraint fk_member_search foreign key (member_id) references member (id)
);

create table store (
    id          bigint auto_increment primary key,
    identifier  varchar(16)  not null,
    name        varchar(255) not null,
    introduce   varchar(255) null,
    profile     varchar(255) null,
    created_at  datetime(6)  null,
    modified_at datetime(6)  null,
    is_deleted  tinyint      null
);

create table product_board (
    id           bigint auto_increment primary key,
    store_id     bigint            not null,
    title        varchar(50)       not null,
    price        int               not null,
    status       tinyint           not null,
    profile      varchar(255)      null,
    purchase_url varchar(255)      not null,
    view         int     default 0 not null,
    sunday       tinyint default 0 not null,
    monday       tinyint default 0 not null,
    tuesday      tinyint default 0 not null,
    wednesday    tinyint default 0 not null,
    thursday     tinyint default 0 not null,
    friday       tinyint default 0 not null,
    saturday     tinyint default 0 not null,
    created_at   datetime(6)       null,
    modified_at  datetime(6)       null,
    is_deleted   tinyint           null,
    wish_cnt     int               null,
    constraint fk_store_board foreign key (store_id) references store (id)
);

create table product_detail (
    id               bigint auto_increment primary key,
    product_board_id bigint       null,
    img_index        int          not null,
    url              varchar(255) not null,
    constraint fk_board_product_detail foreign key (product_board_id) references product_board (id)
);

create table product (
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
     constraint fk_board_product foreign key (product_board_id) references product_board (id)
);

create table product_img (
     id               bigint auto_increment primary key,
     product_board_id bigint       not null,
     url              varchar(255) not null,
     constraint fk_board_product_img foreign key (product_board_id) references product_board (id)
);

create table wishlist_folder (
    id          bigint auto_increment primary key,
    member_id   bigint      not null,
    folder_name varchar(50) null,
    created_at  datetime(6) null,
    modified_at datetime(6) null,
    is_deleted  tinyint     null,
    constraint fk_member_wishlist_folder foreign key (member_id) references member (id)
);

create table wishlist_product (
    id                 bigint auto_increment primary key,
    wishlist_folder_id bigint      not null,
    product_board_id   bigint      not null,
    created_at         datetime(6) null,
    modified_at        datetime(6) null,
    is_deleted         tinyint     null,
    member_id          bigint      null,
    constraint fk_board_wishlist_product foreign key (product_board_id) references product_board (id),
    constraint fk_wishlist_folder_wishlist_product foreign key (wishlist_folder_id) references wishlist_folder (id)
);

create table wishlist_store (
    id          bigint auto_increment primary key,
    member_id   bigint      not null,
    store_id    bigint      not null,
    created_at  datetime(6) null,
    modified_at datetime(6) null,
    is_deleted  tinyint     null,
    constraint fk_member_wishlist_store foreign key (member_id) references member (id),
    constraint fk_store_wishlist_store foreign key (store_id) references store (id)
);

