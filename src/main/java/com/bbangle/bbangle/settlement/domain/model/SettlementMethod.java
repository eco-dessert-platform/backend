package com.bbangle.bbangle.settlement.domain.model;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementMethod {

    // 계좌이체
    BANK_TRANSFER("계좌이체"),
    // 충전금 차감
    DEDUCT_FROM_BALANCE("충전금 차감"),
    // 충전금 적립
    ADD_TO_BALANCE("충전금 적립");

    private final String description;

    // description 으로 enum 찾기, 한글 설명으로 상태를 가져오는 메서드
    public static SettlementMethod fromDescription(String desc) {
        return Arrays.stream(values())
            .filter(s -> s.getDescription().equals(desc))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + desc));
    }

}
