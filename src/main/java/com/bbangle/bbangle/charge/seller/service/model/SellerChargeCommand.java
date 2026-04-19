package com.bbangle.bbangle.charge.seller.service.model;

import java.math.BigDecimal;
import lombok.Builder;

/**
 * 판매자 충전금 관련 커맨드(Command) 객체 모음.
 * Controller에서 Service 계층으로 요청 정보를 전달하는 불변 값 객체(record)입니다.
 */
public class SellerChargeCommand {

    /**
     * 충전금 출금 신청 커맨드.
     * 판매자가 보유한 충전금을 지정 계좌로 출금 신청할 때 사용합니다.
     */
    @Builder
    public record WithdrawalCommand(
        Long sellerId,
        String transactionId,
        BigDecimal withdrawalAmount,
        String bankName,
        String accountHolder,
        String accountNumber
    ) {

    }
}
