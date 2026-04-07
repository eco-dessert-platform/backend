-- Charge Balance 테이블: 판매자 충전금 잔액 관리
CREATE TABLE charge_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
    seller_id BIGINT NOT NULL UNIQUE COMMENT '판매자 ID',
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0 COMMENT '현재 충전금 잔액',
    total_deposited DECIMAL(15, 2) NOT NULL DEFAULT 0 COMMENT '누적 입금액',
    total_withdrawn DECIMAL(15, 2) NOT NULL DEFAULT 0 COMMENT '누적 출금액',
    created_at DATETIME(6) NOT NULL COMMENT '생성일시',
    modified_at DATETIME(6) NOT NULL COMMENT '수정일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='판매자 충전금 잔액';

-- Charge History 테이블: 충전금 거래내역
CREATE TABLE charge_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
    seller_id BIGINT NOT NULL COMMENT '판매자 ID',
    category VARCHAR(20) NOT NULL COMMENT '거래 구분 (ACCUMULATE=적립/DEDUCT=소진)',
    amount DECIMAL(15, 2) NOT NULL COMMENT '거래 금액',
    balance_after DECIMAL(15, 2) NOT NULL COMMENT '거래 후 잔액',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '거래 상태 (PENDING/COMPLETED)',
    base_date DATE NOT NULL COMMENT '거래 발생 일자',
    reference_type VARCHAR(30) NOT NULL COMMENT '참조 타입 (SETTLEMENT/WITHDRAWAL)',
    reference_id BIGINT NOT NULL COMMENT '참조 ID (정산ID 또는 출금신청ID)',
    created_at DATETIME(6) NOT NULL COMMENT '생성일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='충전금 거래내역';

-- Charge Withdrawal Request 테이블: 충전금 출금 신청
CREATE TABLE charge_withdrawal_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
    seller_id BIGINT NOT NULL COMMENT '판매자 ID',
    withdrawal_amount DECIMAL(15, 2) NOT NULL COMMENT '출금 신청 금액',
    bank_name VARCHAR(30) NOT NULL COMMENT '은행명',
    account_holder VARCHAR(30) NOT NULL COMMENT '예금주명',
    account_number VARCHAR(20) NOT NULL COMMENT '계좌번호',
    success TINYINT(1) NOT NULL DEFAULT 0 COMMENT '출금 처리 성공 여부',
    created_at DATETIME(6) NOT NULL COMMENT '생성일시',
    modified_at DATETIME(6) NOT NULL COMMENT '수정일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='충전금 출금 신청';
