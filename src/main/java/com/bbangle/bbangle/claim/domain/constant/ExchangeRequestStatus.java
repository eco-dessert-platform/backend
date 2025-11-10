package com.bbangle.bbangle.claim.domain.constant;

public enum ExchangeRequestStatus {
    REQUESTED,         // 고객이 교환 요청함
    PICKUP_SCHEDULED,  // 교환 수거 예정
    PICKED_UP,         // 교환 상품 수거 완료
    INSPECTING,        // 교환 상품 검수 중
    APPROVED,          // 교환 승인 (새 상품 발송 가능)
    REJECTED,          // 교환 거절
    RESHIPPED,         // 교환 상품 재발송 완료
    COMPLETED;         // 고객 수령 및 교환 완료
}