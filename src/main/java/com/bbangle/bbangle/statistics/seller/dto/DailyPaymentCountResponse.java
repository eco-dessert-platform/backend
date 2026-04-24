package com.bbangle.bbangle.statistics.seller.dto;

import com.bbangle.bbangle.statistics.domain.model.StatisticsPeriod;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

/**
 * 판매자 결제건수/구매자수 통계 응답 DTO.
 * 주문 수와 구매자 수를 같은 버킷 기준으로 누적한 결과를 담습니다.
 */
@Schema(description = "판매자 결제건수 및 구매자수 통계 응답")
public record DailyPaymentCountResponse(
    @Schema(description = "조회 시작일", example = "2026-03-01")
    LocalDate startDate,

    @Schema(description = "조회 종료일", example = "2026-03-07")
    LocalDate endDate,

    @Schema(description = "집계 단위", example = "DAY")
    StatisticsPeriod period,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "버킷 평균 구매자수", example = "1", nullable = true)
    Long averageBuyerCount,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "버킷 평균 결제건수", example = "1", nullable = true)
    Long averagePaymentCount,

    @Schema(description = "버킷별 결제건수/구매자수 목록")
    List<DailyPaymentCountItem> dailyCounts
) {

    /**
     * 하나의 버킷(일/주/월 시작일 기준)에 대한 구매자수/결제건수 항목.
     */
    @Schema(description = "버킷별 결제건수/구매자수 항목")
    public record DailyPaymentCountItem(
        @Schema(description = "버킷 시작일", example = "2026-03-01")
        LocalDate date,

        @Schema(description = "구매자수 합계", example = "1")
        Long buyerCount,

        @Schema(description = "결제건수 합계", example = "2")
        Long paymentCount
    ) {
    }
}
