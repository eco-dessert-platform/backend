package com.bbangle.bbangle.store.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreNameRejectReason {

    ADMIN_INAPPROPRIATE("운영정책 또는 서비스 품질 유지를 위해 부적합"),
    BRAND_NAME_MISUSE("등록된 상표 또는 타 브랜드 명칭 무단 사용"),
    OFFICIAL_STORE_CONFUSION("공식몰로 오인될 가능성"),
    INAPPROPRIATE_LANGUAGE("비속어 또는 부적절한 표현"),
    PRODUCT_CATEGORY_NAME("상품명 또는 카테고리명 포함"),
    CONTACT_INFO_INCLUDED("전화번호, 이메일, URL 등 연락처 포함"),
    ADVERTISING_PHRASE("광고성 문구 포함"),
    SIMILAR_TO_EXISTING_STORE("기존 스토어명과 유사"),
    ETC("기타 사유 (직접 입력)");

    private final String description;
}
