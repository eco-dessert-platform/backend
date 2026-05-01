-- 외부 유입 추적 링크 테이블 (destination URL은 환경별 yml에서 @Value로 주입)
CREATE TABLE tracking_link
(
    id          BIGINT AUTO_INCREMENT,
    channel     VARCHAR(50)  NOT NULL,
    description VARCHAR(255) NULL,
    created_at  DATETIME(6)  NULL,
    modified_at DATETIME(6)  NULL,
    CONSTRAINT tracking_link_pk PRIMARY KEY (id),
    CONSTRAINT uk_tracking_link_channel UNIQUE (channel)
);
