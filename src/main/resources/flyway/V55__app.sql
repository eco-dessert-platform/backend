-- 추적 링크 방문 기록 테이블
CREATE TABLE link_visit
(
    id               BIGINT AUTO_INCREMENT,
    tracking_link_id BIGINT       NOT NULL,
    visitor_hash     VARCHAR(64)  NOT NULL,
    visit_date       DATE         NOT NULL,
    is_duplicate     TINYINT      NOT NULL DEFAULT 0,
    referer          VARCHAR(500) NULL,
    user_agent       VARCHAR(500) NULL,
    created_at       DATETIME(6)  NULL,
    CONSTRAINT link_visit_pk PRIMARY KEY (id),
    INDEX idx_link_visit_dedup (tracking_link_id, visitor_hash, visit_date),
    INDEX idx_link_visit_link_date (tracking_link_id, visit_date),
    CONSTRAINT fk_tracking_link_link_visit FOREIGN KEY (tracking_link_id) REFERENCES tracking_link (id)
);
