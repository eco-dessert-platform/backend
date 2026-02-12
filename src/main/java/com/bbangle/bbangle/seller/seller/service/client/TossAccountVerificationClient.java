package com.bbangle.bbangle.seller.seller.service.client;

import org.springframework.stereotype.Component;

@Component
public class TossAccountVerificationClient implements AccountVerificationClient {

    @Override
    public String verifyAccount(String bankCode, String accountNumber) {
        // TODO: 토스페이먼츠 API 연동 후 실제 구현 예정
        // 현재는 Fake 예금주명 반환
        return "테스트예금주";
    }
}
