package com.bbangle.bbangle.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardType {

    SHINHAN("신한"),
    KB("국민"),
    HYUNDAI("현대"),
    SAMSUNG("삼성"),
    LOTTE("롯데"),
    HANA("하나"),
    WOORI("우리"),
    NH("농협"),
    BC("BC"),
    CITI("씨티"),
    KAKAO("카카오뱅크"),
    TOSS("토스뱅크"),
    ETC("기타");

    private final String description;

}
