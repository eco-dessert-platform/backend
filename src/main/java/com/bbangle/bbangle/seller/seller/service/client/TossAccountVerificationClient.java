package com.bbangle.bbangle.seller.seller.service.client;

import org.springframework.stereotype.Component;

@Component
public class TossAccountVerificationClient implements AccountVerificationClient {

    /**
     * Verifies an account and returns the account holder's name.
     *
     * Currently returns a placeholder name; intended to call Toss Payments API for real verification.
     *
     * @param bankCode      the bank identifier code for the account
     * @param accountNumber the account number to verify
     * @return the account holder's name (placeholder until external verification is implemented)
     */
    @Override
    public String verifyAccount(String bankCode, String accountNumber) {
        // TODO: 토스페이먼츠 API 연동 후 실제 구현 예정
        // 현재는 Fake 예금주명 반환
        return "테스트예금주";
    }
}