package com.bbangle.bbangle.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {

    PENDING("결제 대기"),       // 주문 생성 직후 결제 시도 전
    IN_PROGRESS("결제 중"),     // 결제 프로세스 진행 중
    COMPLETED("결제 완료"),     // 결제 성공
    FAILED("결제 실패"),        // 결제 오류 또는 실패
    CANCELED("결제 취소"),      // 사용자 또는 관리자에 의한 취소
    REFUNDED("환불 완료");      // 결제 취소 후 환불 처리 완료

    private final String description;

}
