package com.bbangle.bbangle.analytics.controller;


import com.bbangle.bbangle.analytics.dto.AnalyticsCountWithDateResponseDto;
import com.bbangle.bbangle.analytics.dto.AnalyticsRatioWithDateResponseDto;
import com.bbangle.bbangle.analytics.service.AnalyticsService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ResponseService responseService;
    private final AnalyticsService analyticsService;


    @GetMapping(value = "/members/count")
    public CommonResult getMembersCount() {
        return responseService.getSingleResult(analyticsService.countAllMembers());
    }


    @GetMapping(value = "/new-members/count")
    public CommonResult getNewMembersCount(
        @RequestParam(value = "startDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<LocalDate> startDate,
        @RequestParam(value = "endDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<LocalDate> endDate
    ) {
        List<AnalyticsCountWithDateResponseDto> response = analyticsService.countMembersByPeriod(
            startDate, endDate);
        return responseService.getListResult(response);
    }


    @GetMapping(value = "/ratio/wishlist-usage")
    public CommonResult getWishlistUsageRatio(
        @RequestParam(value = "startDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<LocalDate> startDate,
        @RequestParam(value = "endDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<LocalDate> endDate
    ) {
        List<AnalyticsRatioWithDateResponseDto> response = analyticsService.calculateWishlistUsingRatio(
            startDate, endDate);
        return responseService.getListResult(response);
    }


    @GetMapping(value = "/wishlist/boards/count")
    public CommonResult getWishlistUsageCount(
        @RequestParam(value = "startDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<LocalDate> startDate,
        @RequestParam(value = "endDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<LocalDate> endDate
    ) {
        List<AnalyticsCountWithDateResponseDto> response = analyticsService.countWishlistBoardByPeriod(
            startDate, endDate);
        return responseService.getListResult(response);
    }


    @GetMapping(value = "/ratio/review-usage")
    public CommonResult getReviewUsageRatio(
        @RequestParam(value = "startDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<LocalDate> startDate,
        @RequestParam(value = "endDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<LocalDate> endDate
    ) {
        List<AnalyticsRatioWithDateResponseDto> response = analyticsService.calculateReviewUsingRatio(
            startDate, endDate);
        return responseService.getListResult(response);
    }


    @GetMapping(value = "/reviews/count")
    public CommonResult getReviewUsageCount(
        @RequestParam(value = "startDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<LocalDate> startDate,
        @RequestParam(value = "endDate")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Optional<LocalDate> endDate
    ) {
        List<AnalyticsCountWithDateResponseDto> response = analyticsService.countReviewByPeriod(
            startDate, endDate);
        return responseService.getListResult(response);
    }

}
