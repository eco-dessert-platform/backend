package com.bbangle.bbangle.statistics.seller.dto;

import com.bbangle.bbangle.statistics.domain.model.StatisticsPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 판매자 환불율 통계 응답 DTO.
 * 일별 적재 데이터를 기준으로 화면 버킷(DAY/WEEK/MONTH) 단위 응답을 구성합니다.
 */
@Schema(description = "판매자 환불율 통계 응답")
public record DailyRefundRateResponse(
    @Schema(description = "조회 시작일", example = "2026-03-01")
    LocalDate startDate,

    @Schema(description = "조회 종료일", example = "2026-03-07")
    LocalDate endDate,

    @Schema(description = "집계 단위", example = "DAY")
    StatisticsPeriod period,

    @Schema(description = "버킷 평균 환불율(%)", example = "35.42", nullable = true)
    BigDecimal averageRefundRate,

    @Schema(description = "버킷별 환불 통계 목록")
    List<DailyRefundRateItem> dailyRefundRates
) {

    /**
     * 하나의 버킷(일/주/월 시작일 기준)에 대한 환불 통계 항목.
     */
    @Schema(description = "버킷별 환불 통계 항목")
    public record DailyRefundRateItem(
        @Schema(description = "버킷 시작일", example = "2026-03-01")
        LocalDate date,

        @Schema(description = "결제금액 합계", example = "12000")
        long paymentAmount,

        @Schema(description = "환불금액 합계", example = "3000")
        long refundAmount,

        @Schema(description = "결제금액 대비 환불율(%)", example = "25.00")
        BigDecimal refundRate
    ) {
    }
}
