package com.bbangle.bbangle.order.domain.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
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

    // ---- 화면 집계용 그룹핑 상수 ----
    public static final Set<OrderStatus> PAYMENT_COMPLETED_GROUP =
        EnumSet.of(PAYMENT_COMPLETED);

    public static final Set<OrderStatus> ORDER_CONFIRMED_GROUP =
        EnumSet.of(ORDER_CONFIRMED, IN_PRODUCTION);

    public static final Set<OrderStatus> SHIPPED_GROUP =
        EnumSet.of(SHIPPED);

    public static final Set<OrderStatus> DELIVERY_COMPLETED_GROUP =
        EnumSet.of(PURCHASE_CONFIRMED);

    public static final Set<OrderStatus> CANCELLED_GROUP =
        EnumSet.of(CANCEL_REQUESTED, CANCEL_APPROVED, CANCEL_REJECTED);

    /** 새로운 반품 상태 추가 시 이 Set에도 반드시 추가해야 합니다. */
    public static final Set<OrderStatus> RETURNED_GROUP =
        EnumSet.of(
            RETURN_REQUESTED, RETURN_APPROVED, RETURN_REJECTED,
            RETURN_PICKUP_IN_PROGRESS, RETURN_INSPECTION, RETURN_PROCESSING,
            RETURN_RETURNED, RETURN_ON_HOLD, RETURN_COMPLETED
        );

    /** 새로운 교환 상태 추가 시 이 Set에도 반드시 추가해야 합니다. */
    public static final Set<OrderStatus> EXCHANGED_GROUP =
        EnumSet.of(
            EXCHANGE_REQUEST, EXCHANGE_APPROVED, EXCHANGE_REJECTED,
            EXCHANGE_ITEM_COLLECTED, EXCHANGE_ITEM_INSPECTING, EXCHANGE_IN_PROGRESS,
            EXCHANGE_RETURNED, EXCHANGE_ON_HOLD, EXCHANGE_ITEM_SHIPPED, EXCHANGE_COMPLETED
        );

    // description 으로 enum 찾기, 한글 설명으로 상태를 가져오는 메서드
    public static OrderStatus fromDescription(String desc) {
        return Arrays.stream(values())
            .filter(s -> s.getDescription().equals(desc))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown status: " + desc));
    }
}
