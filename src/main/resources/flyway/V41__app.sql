create table if not exists store_name_request (
    id                    bigint auto_increment  primary key,
    current_name          varchar(255)                             not null,
    new_name              varchar(255)                             not null,
    status                varchar(20) default 'PENDING'            not null,
    reject_reason         varchar(30)                              null,
    reject_detail         varchar(255)                             null,
    created_at            datetime(6) default current_timestamp(6),
    seller_id             bigint                                   not null,
    store_id              bigint                                   not null,
    CONSTRAINT fk_seller_store_name_request FOREIGN KEY (seller_id) REFERENCES sellers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_store_store_name_request FOREIGN KEY (store_id) REFERENCES store(id) ON DELETE RESTRICT
);

create index idx_store_name_request_status_created_at on store_name_request(status, created_at desc);