package com.bbangle.bbangle.fixture.settlement.domain;

import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.settlement.domain.DailySettlement;
import com.bbangle.bbangle.settlement.domain.model.SettlementMethod;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class DailySettlementFixture {

    private DailySettlementFixture() {
    }

    public static DailySettlement defaultDailySettlement(Seller seller) {
        return DailySettlement.builder()
            .seller(seller)
            .settlementNumber("250401A1F7")
            .scheduledDate(LocalDate.of(2025, 3, 1))
            .completedDate(LocalDate.of(2025, 3, 1))
            .amount(new BigDecimal("100000"))
            .fee(new BigDecimal("-3000"))
            .deductibleRefund(new BigDecimal("-2000"))
            .withHoldingPayment(BigDecimal.ZERO)
            .settlementMethod(SettlementMethod.BANK_TRANSFER)
            .deliveryFeeChange(new BigDecimal("-1500"))
            .balanceOffset(new BigDecimal("-500"))
            .build();
    }

    public static DailySettlement create(
        Seller seller,
        String settlementNumber,
        LocalDate scheduledDate,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal deductibleRefund,
        BigDecimal withHoldingPayment
    ) {
        return DailySettlement.builder()
            .seller(seller)
            .settlementNumber(settlementNumber)
            .scheduledDate(scheduledDate)
            .completedDate(scheduledDate)
            .amount(amount)
            .fee(fee)
            .deductibleRefund(deductibleRefund)
            .withHoldingPayment(withHoldingPayment)
            .settlementMethod(SettlementMethod.BANK_TRANSFER)
            .deliveryFeeChange(BigDecimal.ZERO)
            .balanceOffset(BigDecimal.ZERO)
            .build();
    }

}
