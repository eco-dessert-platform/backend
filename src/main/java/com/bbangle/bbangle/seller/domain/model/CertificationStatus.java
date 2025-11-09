package com.bbangle.bbangle.seller.domain.model;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CertificationStatus {

    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String description;


    public static CertificationStatus fromDescription(String desc) {
        return Arrays.stream(values())
            .filter(s -> s.getDescription().equals(desc))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + desc));
    }

}
