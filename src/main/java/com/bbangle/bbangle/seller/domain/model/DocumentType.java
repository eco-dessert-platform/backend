package com.bbangle.bbangle.seller.domain.model;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentType {

    BUSINESS_REGISTRATION_CERTIFICATE("Business Registration Certificate", "사업자등록증"),
    MAIL_ORDER_SALES_REPORT("Mail Order Sales Report", "통신판매업신고증"),
    INSTANT_FOOD_MANUFACTURING_PROCESSING_REGISTRATION(
        "Instant Food Manufacturing Processing Registration", "즉석식품제조가공업등록증"),
    BANKBOOK_COPY("Bankbook Copy", "통장사본");

    private final String description;
    private final String koreanName;


    public static DocumentType fromDescription(String desc) {
        return Arrays.stream(values())
            .filter(s -> s.getDescription().equals(desc))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown document type: " + desc));
    }

}
