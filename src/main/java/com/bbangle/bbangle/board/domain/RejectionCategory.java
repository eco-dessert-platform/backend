package com.bbangle.bbangle.board.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RejectionCategory {

    ADMIN_JUDGMENT("관리자 판단 부재함"),
    INAPPROPRIATE_BRAND_NAME("브랜드명 부적절 사용"),
    INVALID_PRICE_CONDITION("공지사항 가격 조건 위반"),
    INAPPROPRIATE_VEGAN_EXPRESSION("비거니/비건 표현 부적합"),
    PROHIBITED_STORE_EXPRESSION("상품명/카테고리 포함 금지 표현"),
    PROHIBITED_LOGO_TEXT("로고사용 문구 포함"),
    CONTAINS_CONTACT_INFO("연락처/사이트 포함"),
    CONTAINS_COMPETITOR_NAME("타 판매자명 공유"),
    DIRECT_INPUT("직접입력");

    private final String description;
}
