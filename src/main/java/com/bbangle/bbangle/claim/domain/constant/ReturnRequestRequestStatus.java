package com.bbangle.bbangle.claim.domain.constant;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;

public enum ReturnRequestRequestStatus {
    REQUESTED,           // 고객이 반품 요청함
    APPROVED,            // 판매자 초기 승인 — 수거 예약 진행 가능
    PICKUP_SCHEDULED,    // 반품 수거 예정
    PICKED_UP,           // 반품 수거 완료
    INSPECTING,          // 반품 상품 검수 중
    INSPECTION_APPROVED, // 검수 후 최종 승인 — 환불 진행 가능
    HOLD,                // 추가 확인 필요로 보류됨
    REJECTED,            // 반품 거절
    COMPLETED;           // 환불까지 완료됨

    public void validateTransition(ReturnRequestRequestStatus next) {
        if (!canTransitionTo(next)) {
            throw new BbangleException(BbangleErrorCode.CLAIM_INVALID_STATUS);
        }
    }

    private boolean canTransitionTo(ReturnRequestRequestStatus next) {
        return switch (this) {
            case APPROVED            -> next == PICKUP_SCHEDULED;
            case PICKUP_SCHEDULED    -> next == PICKED_UP;
            case PICKED_UP           -> next == INSPECTING;
            case INSPECTING          -> next == INSPECTION_APPROVED || next == REJECTED;
            case INSPECTION_APPROVED -> next == COMPLETED;
            default -> false;
        };
    }
}
