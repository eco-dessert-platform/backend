package com.bbangle.bbangle.board.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RejectionCategory {

    ADMIN_JUDGMENT("관리자 판단 부적합"),
    INAPPROPRIATE_BRAND_NAME("브랜드명 무단사용"),
    OFFICIAL_MATERIAL_CONFUSION("공식물 오인 가능"),
    INAPPROPRIATE_EXPRESSION("비속어/부적절한 표현"),
    PROHIBITED_STORE_EXPRESSION("상품명/카테고리명 포함"),
    CONTAINS_ADVERTISING("광고성 문구 포함"),
    CONTAINS_CONTACT_INFO("연락처/URL 포함"),
    CONTAINS_COMPETITOR_NAME("타 판매자명 유사"),
    DIRECT_INPUT("직접입력");

    private final String description;
}
