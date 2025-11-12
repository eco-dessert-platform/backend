-- ----------------------------
-- Settlement 관련 테이블 생성 (준규님)
-- ----------------------------

CREATE TABLE daily_settlement (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  scheduled_date DATE,                           -- 정산 예정일
                                  completed_date DATE,                           -- 정산 완료일
                                  amount DECIMAL(15,2),                          -- 결제 금액
                                  fee DECIMAL(15,2),                             -- 수수료
                                  deductible_refund DECIMAL(15,2),               -- 공제/환급
                                  with_holding_payment DECIMAL(15,2),            -- 지급보류
                                  settlement_method VARCHAR(20),                 -- 정산 방식
                                  created_at DATETIME(6) NOT NULL,
                                  modified_at DATETIME(6) NOT NULL,
                                  is_deleted TINYINT(1) DEFAULT 0
);


CREATE TABLE settlement_item (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 settlement_number VARCHAR(50),                 -- 정산번호
                                 type VARCHAR(20),                              -- 정산 타입
                                 scheduled_amount DECIMAL(15,2),                -- 예정 금액
                                 base_date DATE,                                -- 기준일
                                 scheduled_date DATE,                           -- 예정일
                                 completed_date DATE,                           -- 완료일
                                 status VARCHAR(20),                            -- 상태 (Enum: SettlementStatus)
                                 daily_settlement_id BIGINT,                    -- FK: 일일 정산
                                 order_item_id BIGINT,                          -- FK: 주문상품
                                 created_at DATETIME(6) NOT NULL,
                                 modified_at DATETIME(6) NOT NULL,
                                 is_deleted TINYINT(1) DEFAULT 0,
                                 CONSTRAINT fk_settlement_item_daily_settlement FOREIGN KEY (daily_settlement_id)
                                     REFERENCES daily_settlement (id),
                                 CONSTRAINT fk_settlement_item_order_item FOREIGN KEY (order_item_id)
                                     REFERENCES order_item (id)
);
