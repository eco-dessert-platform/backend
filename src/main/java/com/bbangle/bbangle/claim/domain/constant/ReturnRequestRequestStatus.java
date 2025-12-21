package com.bbangle.bbangle.claim.domain.constant;

public enum ReturnRequestRequestStatus {
    REQUESTED,        // 고객이 반품 요청함
    PICKUP_SCHEDULED, // 반품 수거 예정
    PICKED_UP,        // 반품 수거 완료
    INSPECTING,       // 반품 상품 검수 중
    APPROVED,         // 반품 승인 (환불 진행 가능)
    REJECTED,         // 반품 거절
    COMPLETED;        // 환불까지 완료됨
}
