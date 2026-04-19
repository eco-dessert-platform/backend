ALTER TABLE charge_withdrawal_request
    ADD COLUMN failure_reason VARCHAR(255) COMMENT '출금 실패 이유' AFTER success;