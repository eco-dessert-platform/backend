package com.bbangle.bbangle.statistics.seller.controller.swagger;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.statistics.domain.model.StatisticsPeriod;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentAmountResponse;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentCountResponse;
import com.bbangle.bbangle.statistics.seller.dto.DailyRefundRateResponse;
import com.bbangle.bbangle.statistics.seller.dto.WeekdayPaymentAmountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Optional;

@Tag(name = "Seller Payment Statistics", description = "판매자 결제 통계 API")
public interface SellerPaymentStatisticsApi {

    @Operation(summary = "판매자 결제 금액 통계 조회")
    SingleResult<DailyPaymentAmountResponse> getDailyPaymentAmount(
        @Parameter(description = "판매자 ID") Long sellerId,
        @Parameter(description = "기준 날짜 (yyyy-MM-dd), 미입력 시 오늘") Optional<LocalDate> date,
        @Parameter(description = "조회 범위 (DAY, WEEK, MONTH), 미입력 시 DAY") Optional<StatisticsPeriod> period
    );

    @Operation(
        summary = "판매자 결제 건수 및 결제자 수 통계 조회",
        description = "결제 건수와 결제자 수를 일별, 주별, 월별 기준으로 조회합니다."
    )
    SingleResult<DailyPaymentCountResponse> getDailyPaymentCount(
        @Parameter(description = "판매자 ID") Long sellerId,
        @Parameter(description = "기준 날짜 (yyyy-MM-dd), 미입력 시 오늘") Optional<LocalDate> date,
        @Parameter(description = "조회 범위 (DAY, WEEK, MONTH), 미입력 시 DAY") Optional<StatisticsPeriod> period
    );

    @Operation(
        summary = "판매자 요일별 결제 금액 및 평균 결제 금액 통계 조회",
        description = "조회 기간 데이터를 요일 기준으로 수집해 요일별 총 결제 금액과 평균 결제 금액을 조회합니다."
    )
    SingleResult<WeekdayPaymentAmountResponse> getWeekdayPaymentAmount(
        @Parameter(description = "판매자 ID") Long sellerId,
        @Parameter(description = "기준 날짜 (yyyy-MM-dd), 미입력 시 오늘") Optional<LocalDate> date,
        @Parameter(description = "조회 범위 (DAY, WEEK, MONTH), 미입력 시 DAY") Optional<StatisticsPeriod> period
    );

    @Operation(
        summary = "판매자 환불율 통계 조회",
        description = "환불 처리일 기준으로 일별, 주별, 월별 환불 금액과 환불율을 조회합니다."
    )
    SingleResult<DailyRefundRateResponse> getDailyRefundRate(
        @Parameter(description = "판매자 ID") Long sellerId,
        @Parameter(description = "기준 날짜 (yyyy-MM-dd), 미입력 시 오늘") Optional<LocalDate> date,
        @Parameter(description = "조회 범위 (DAY, WEEK, MONTH), 미입력 시 DAY") Optional<StatisticsPeriod> period
    );
}
