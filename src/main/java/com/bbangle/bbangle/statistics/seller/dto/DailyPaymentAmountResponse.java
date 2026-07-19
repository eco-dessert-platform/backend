package com.bbangle.bbangle.statistics.seller.dto;

import com.bbangle.bbangle.statistics.domain.model.StatisticsPeriod;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/**
 * 판매자 결제금액 통계 응답 DTO.
 * 일별 적재 데이터를 조회 화면의 버킷 단위로 합산한 결과를 담습니다.
 */
@Schema(description = "판매자 결제금액 통계 응답")
public record DailyPaymentAmountResponse(
    @Schema(description = "조회 시작일", example = "2026-03-01")
    LocalDate startDate,

    @Schema(description = "조회 종료일", example = "2026-03-07")
    LocalDate endDate,

    @Schema(description = "집계 단위", example = "DAY")
    StatisticsPeriod period,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "버킷 평균 결제금액", example = "1429", nullable = true)
    Long averageAmount,

    @Schema(description = "버킷별 결제금액 목록")
    List<DailyPaymentAmountItem> dailyAmounts
) {

    /**
     * 하나의 버킷(일/주/월 시작일 기준)에 대한 결제금액 항목.
     */
    @Schema(description = "버킷별 결제금액 항목")
    public record DailyPaymentAmountItem(
        @Schema(description = "버킷 시작일", example = "2026-03-01")
        LocalDate date,

        @Schema(description = "결제금액 합계", example = "12000")
        Long amount
    ) {
    }
}
