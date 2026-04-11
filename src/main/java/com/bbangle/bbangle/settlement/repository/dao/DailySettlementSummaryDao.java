package com.bbangle.bbangle.settlement.repository.dao;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 일별 정산 요약 집계 결과 (QueryDSL 조회 전용)
 * 정산예정일 범위(min/max)와 총 정산 금액을 담는 DAO
 */
public record DailySettlementSummaryDao(
    LocalDate scheduledDateMin,
    LocalDate scheduledDateMax,
    BigDecimal totalSettlementAmount
) {

}
