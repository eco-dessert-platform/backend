package com.bbangle.bbangle.claim.domain.constant;

public enum CancelRequestStatus {
    REQUESTED,   // 고객이 취소 요청
    APPROVED,    // 취소 승인함
    REJECTED,    // 취소 요청이 거절
    COMPLETED;   // 취소 완료
}
