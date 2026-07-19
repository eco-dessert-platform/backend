package com.bbangle.bbangle.statistics.seller.dto;

import com.bbangle.bbangle.statistics.domain.model.StatisticsPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/**
 * 판매자 요일별 결제금액 통계 응답 DTO.
 * 조회 기간의 일별 데이터를 월~일 요일 기준으로 재분류한 결과를 담습니다.
 */
@Schema(description = "판매자 요일별 결제금액 통계 응답")
public record WeekdayPaymentAmountResponse(
    @Schema(description = "조회 시작일", example = "2026-03-01")
    LocalDate startDate,

    @Schema(description = "조회 종료일", example = "2026-03-07")
    LocalDate endDate,

    @Schema(description = "집계 단위", example = "DAY")
    StatisticsPeriod period,

    @Schema(description = "요일별 결제금액 목록")
    List<WeekdayPaymentAmountItem> weekdayAmounts
) {

    /**
     * 하나의 요일에 대한 결제금액 통계 항목.
     */
    @Schema(description = "요일별 결제금액 항목")
    public record WeekdayPaymentAmountItem(
        @Schema(description = "요일 번호(월=1, 일=7)", example = "1")
        Integer weekday,

        @Schema(description = "해당 요일 총 결제금액", example = "4000")
        Long amount,

        @Schema(description = "해당 요일 평균 결제금액", example = "571")
        Long averageAmount
    ) {
    }
}
