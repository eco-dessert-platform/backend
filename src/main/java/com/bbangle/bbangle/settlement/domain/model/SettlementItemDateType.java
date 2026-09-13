package com.bbangle.bbangle.settlement.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "건별 정산 내역 조회 기준 날짜")
public enum SettlementItemDateType {

    @Schema(description = "정산예정일 (SettlementItem.scheduledDate)")
    SCHEDULED_DATE,

    @Schema(description = "정산기준일 (SettlementItem.baseDate)")
    BASE_DATE,

    @Schema(description = "정산완료일 (SettlementItem.completedDate)")
    COMPLETED_DATE

}
