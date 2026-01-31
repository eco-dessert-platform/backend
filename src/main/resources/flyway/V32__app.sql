-- account_verifications 테이블의 bank_name 컬럼을 bank_code로 변경
ALTER TABLE account_verifications CHANGE COLUMN bank_name bank_code VARCHAR(10);
