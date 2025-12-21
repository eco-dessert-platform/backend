package com.bbangle.bbangle.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {

    CARD("신용/체크카드"),
    BANK_TRANSFER("무통장입금"),
    VIRTUAL_ACCOUNT("가상계좌"),
    MOBILE("휴대폰결제"),
    NAVER_PAY("네이버페이"),
    KAKAO_PAY("카카오페이"),
    TOSS_PAY("토스페이"),
    POINT("포인트결제");

    private final String description;

}
