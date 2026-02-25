package com.bbangle.bbangle.store.domain.model;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreStatus {
    
    // 크롤링 하여 수집한 가게는 아직 판매자가 등록되어있지 않으니 비선점
    // 판매자가 만약 가게를 등록하면 해당 가게는 선점
    // 관리자가 승인하면 해당 가게는 등록
    // 관리자가 거절하면 해당 가게는 다시 비선점

    RESERVED("선점"), // 판매자가 해당 가게를 등록했지만 관리자가 승인하지 않은 상태
    ACTIVE("등록"),   // 관리자가 승인하여 최종적으로 해당 가게의 판매자가 된 상태
    NONE("비선점");    // 아직 판매자가 등록하지 않은 상태 


    private final String description;


    public static StoreStatus fromDescription(String desc) {
        return Arrays.stream(values())
            .filter(s -> s.getDescription().equals(desc))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown store status: " + desc));
    }


}
