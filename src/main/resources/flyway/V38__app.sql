-- Store Application 테이블 추가
create table if not exists store_application (
    id                    bigint auto_increment  primary key,
    name                  varchar(255)                             not null,
    introduce             varchar(255)                             null,
    profile               varchar(255)                             null,
    status                varchar(20) default 'PENDING'            not null,
    phone                 varchar(20)                              null,
    sub_phone             varchar(20)                              null,
    email                 varchar(100)                             null,
    origin_address_line   varchar(255)                             null,
    origin_address_detail varchar(255)                             null,
    created_at            datetime(6) default current_timestamp(6),
    modified_at           datetime(6) default current_timestamp(6) on update current_timestamp(6),
    seller_id             bigint                                   not null,
    store_id              bigint                                   null,
    CONSTRAINT fk_seller_store_application FOREIGN KEY (seller_id) REFERENCES sellers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_store_store_application FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE RESTRICT
);

-- 관리자가 status = PENDING인 스토어 신청서를 조회
create index idx_store_application_status_created_at on store_application(status, created_at desc);
-- 판매자가 가장 최근에 제출한 스토어 신청서 조회
create index idx_store_application_seller_created_at on store_application(seller_id, created_at desc);

-- Store 테이블의 status 삭제
alter table store drop column if exists status;