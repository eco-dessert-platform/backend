package com.bbangle.bbangle.seller.domain.model;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CertificationStatus {

    NEW("New"), // 사업자 정보 등록 전
    PENDING("Pending"), // 사업자 정보 심사 중
    APPROVED("Approved"),   // 심사 승인
    REJECTED("Rejected");   // 심사 거절

    private final String description;


    public static CertificationStatus fromDescription(String desc) {
        return Arrays.stream(values())
            .filter(s -> s.getDescription().equals(desc))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + desc));
    }

}
