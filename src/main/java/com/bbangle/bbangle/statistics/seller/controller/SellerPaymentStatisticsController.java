package com.bbangle.bbangle.statistics.seller.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.statistics.domain.model.StatisticsPeriod;
import com.bbangle.bbangle.statistics.seller.controller.swagger.SellerPaymentStatisticsApi;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentAmountResponse;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentCountResponse;
import com.bbangle.bbangle.statistics.seller.dto.WeekdayPaymentAmountResponse;
import com.bbangle.bbangle.statistics.seller.service.SellerPaymentStatisticsService;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller/payments/statistics")
public class SellerPaymentStatisticsController implements SellerPaymentStatisticsApi {

    private final ResponseService responseService;
    private final SellerPaymentStatisticsService sellerPaymentStatisticsService;

    @Override
    @GetMapping("/daily-amount")
    public SingleResult<DailyPaymentAmountResponse> getDailyPaymentAmount(
        @AuthenticationPrincipal Long sellerId,
        @RequestParam(value = "date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<LocalDate> date,
        @RequestParam(value = "period", required = false)
        Optional<StatisticsPeriod> period
    ) {
        DailyPaymentAmountResponse result =
            sellerPaymentStatisticsService.getDailyPaymentAmount(sellerId, date, period);
        return responseService.getSingleResult(result);
    }

    @Override
    @GetMapping("/daily-count")
    public SingleResult<DailyPaymentCountResponse> getDailyPaymentCount(
        @AuthenticationPrincipal Long sellerId,
        @RequestParam(value = "date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<LocalDate> date,
        @RequestParam(value = "period", required = false)
        Optional<StatisticsPeriod> period
    ) {
        DailyPaymentCountResponse result =
            sellerPaymentStatisticsService.getDailyPaymentCount(sellerId, date, period);
        return responseService.getSingleResult(result);
    }

    @Override
    @GetMapping("/weekday")
    public SingleResult<WeekdayPaymentAmountResponse> getWeekdayPaymentAmount(
        @AuthenticationPrincipal Long sellerId,
        @RequestParam(value = "date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<LocalDate> date,
        @RequestParam(value = "period", required = false)
        Optional<StatisticsPeriod> period
    ) {
        WeekdayPaymentAmountResponse result =
            sellerPaymentStatisticsService.getWeekdayPaymentAmount(sellerId, date, period);
        return responseService.getSingleResult(result);
    }
}
