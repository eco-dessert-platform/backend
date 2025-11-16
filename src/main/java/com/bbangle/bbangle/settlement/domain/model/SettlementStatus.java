package com.bbangle.bbangle.settlement.domain.model;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementStatus {

    // 정산
    PENDING("정산대기"),
    // 정산 전 취소
    BEFORE_PAYMENT_CANCELLED("정산 전 취소"),
    // 정산 후 취소
    AFTER_PAYMENT_CANCELLED("정산 후 취소");

    private final String description;


    // description 으로 enum 찾기, 한글 설명으로 상태를 가져오는 메서드
    public static SettlementStatus fromDescription(String desc) {
        return Arrays.stream(values())
            .filter(s -> s.getDescription().equals(desc))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + desc));
    }
}
