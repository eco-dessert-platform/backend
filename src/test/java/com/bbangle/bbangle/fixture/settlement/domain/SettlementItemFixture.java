package com.bbangle.bbangle.fixture.settlement.domain;

import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.settlement.domain.DailySettlement;
import com.bbangle.bbangle.settlement.domain.SettlementItem;
import com.bbangle.bbangle.settlement.domain.model.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SettlementItem 테스트 Fixture.
 * 건별 정산 내역 관련 테스트에서 재사용 가능한 기본 빌더를 제공한다.
 */
public final class SettlementItemFixture {

    private SettlementItemFixture() {
    }

    /**
     * 기본 SettlementItem 생성.
     * 전달된 DailySettlement 및 OrderItem과 연결된다.
     */
    public static SettlementItem create(
        DailySettlement dailySettlement,
        OrderItem orderItem,
        BigDecimal scheduledAmount,
        LocalDate baseDate,
        LocalDate scheduledDate,
        SettlementStatus status
    ) {
        return SettlementItem.builder()
            .settlementNumber("SI-" + baseDate)
            .type("NORMAL")
            .scheduledAmount(scheduledAmount)
            .baseDate(baseDate)
            .scheduledDate(scheduledDate)
            .status(status)
            .dailySettlement(dailySettlement)
            .orderItem(orderItem)
            .build();
    }

    /**
     * 정산대기 상태의 기본 SettlementItem 생성.
     */
    public static SettlementItem createPending(
        DailySettlement dailySettlement,
        OrderItem orderItem,
        BigDecimal scheduledAmount,
        LocalDate baseDate
    ) {
        return create(
            dailySettlement,
            orderItem,
            scheduledAmount,
            baseDate,
            baseDate.plusDays(7),  // 정산 예정일은 기준일로부터 7일 후
            SettlementStatus.PENDING
        );
    }

}
