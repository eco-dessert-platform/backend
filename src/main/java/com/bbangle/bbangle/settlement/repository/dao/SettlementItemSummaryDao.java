package com.bbangle.bbangle.settlement.repository.dao;

import java.math.BigDecimal;

/**
 * 건별 정산 요약 집계 결과 (QueryDSL 조회 전용 DAO).
 * 총 건수는 페이징 쿼리에서 이미 계산되므로 금액 합계만 담는다.
 */
public record SettlementItemSummaryDao(
    BigDecimal totalScheduledAmount
) {

}
