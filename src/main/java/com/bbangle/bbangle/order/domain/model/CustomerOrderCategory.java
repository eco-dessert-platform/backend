package com.bbangle.bbangle.order.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 소비자 주문목록 화면에서 사용하는 주문 유형(탭) 구분.
 * 세분화된 {@link OrderStatus} 를 소비자 관점의 4개 카테고리로 묶습니다.
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "소비자 주문 유형")
public enum CustomerOrderCategory {

    NORMAL("일반"),
    RETURN("반품"),
    EXCHANGE("교환"),
    CANCEL("취소");

    private final String description;

    /**
     * OrderStatus 를 소비자 화면 카테고리로 변환합니다.
     * 취소/반품/교환 그룹에 속하지 않는 상태는 모두 일반(NORMAL)으로 간주합니다.
     */
    public static CustomerOrderCategory from(OrderStatus status) {
        if (status == null) {
            return NORMAL;
        }
        if (OrderStatus.CANCELLED_GROUP.contains(status)) {
            return CANCEL;
        }
        if (OrderStatus.RETURNED_GROUP.contains(status)) {
            return RETURN;
        }
        if (OrderStatus.EXCHANGED_GROUP.contains(status)) {
            return EXCHANGE;
        }
        return NORMAL;
    }
}
