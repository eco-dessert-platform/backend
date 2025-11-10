package com.bbangle.bbangle.seller.domain.model;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentType {

    // TODO : 각 문서 형식을 이해하기 쉽게 주석 추가

    // 사업자등록증
    BUSINESS_REGISTRATION_CERTIFICATE("Business Registration Certificate"),
    // 통신판매업신고증
    MAIL_ORDER_SALES_REPORT("Mail Order Sales Report"),
    // 즉석식품제조가공업등록증
    INSTANT_FOOD_MANUFACTURING_PROCESSING_REGISTRATION(
        "Instant Food Manufacturing Processing Registration"),
    // 통장사본
    BANKBOOK_COPY("Bankbook Copy");

    private final String description;


    public static DocumentType fromDescription(String desc) {
        return Arrays.stream(values())
            .filter(s -> s.getDescription().equals(desc))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown document type: " + desc));
    }

}
