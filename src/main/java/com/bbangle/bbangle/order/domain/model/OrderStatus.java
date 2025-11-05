package com.bbangle.bbangle.order.domain.model;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {

    // 주문관리 페이지에서 조회의 효율성을 위해 상태를 세분화함

    // 일반
    PAYMENT_COMPLETED("결제완료"),
    ORDER_CONFIRMED("발주확인"),
    IN_PRODUCTION("상품제작중"),
    SHIPPED("상품발송"),
    PURCHASE_CONFIRMED("구매확정"),

    // 취소
    CANCEL_REQUESTED("취소요청"),
    CANCEL_APPROVED("취소승인"),
    CANCEL_REJECTED("취소반려"),

    // 반품
    RETURN_REQUESTED("반품 요청"),
    RETURN_APPROVED("반품 승인"),
    RETURN_REJECTED("반품 거절"),
    RETURN_PICKUP_IN_PROGRESS("상품 회수 중"),
    RETURN_INSPECTION("상품 확인중"),
    RETURN_PROCESSING("반품 진행"),
    RETURN_RETURNED("반품 반려"),
    RETURN_ON_HOLD("반품 보류"),
    RETURN_COMPLETED("반품 완료"),

    // 교환
    EXCHANGE_REQUEST("교환요청"),
    EXCHANGE_APPROVED("교환 승인"),
    EXCHANGE_REJECTED("교환 거절"),
    EXCHANGE_ITEM_COLLECTED("상품 회수"),
    EXCHANGE_ITEM_INSPECTING("상품 확인중"),
    EXCHANGE_IN_PROGRESS("교환 진행"),
    EXCHANGE_RETURNED("교환 반려"),
    EXCHANGE_ON_HOLD("교환 보류"),
    EXCHANGE_ITEM_SHIPPED("상품 발송"),
    EXCHANGE_COMPLETED("교환 완료");

    private final String description;


    // description 으로 enum 찾기, 한글 설명으로 상태를 가져오는 메서드
    public static OrderStatus fromDescription(String desc) {
        return Arrays.stream(values())
            .filter(s -> s.getDescription().equals(desc))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + desc));
    }
}
