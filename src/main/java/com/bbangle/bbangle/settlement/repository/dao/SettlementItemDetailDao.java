package com.bbangle.bbangle.settlement.repository.dao;

import com.bbangle.bbangle.settlement.domain.model.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 건별 정산 내역 조회 결과 (QueryDSL 조회 전용 DAO)
 * SettlementItem + OrderItem + Order + Product 조인 결과를 담는 레코드
 */
public record SettlementItemDetailDao(
    String orderNumber,
    Long orderItemId,
    Long sellerId,
    String buyerName,
    String productTitle,
    BigDecimal scheduledAmount,
    Integer quantity,
    LocalDate baseDate,
    LocalDate scheduledDate,
    SettlementStatus status
) {

}
