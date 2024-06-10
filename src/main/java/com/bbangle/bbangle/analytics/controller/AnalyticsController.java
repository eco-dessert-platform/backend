package com.bbangle.bbangle.analytics.controller;


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
import java.util.Optional;

@RestController
@RequestMapping("api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ResponseService responseService;
    private final AnalyticsService analyticsService;


    @GetMapping(value = "/members/count")
    public CommonResult getMembersCount() {
        return responseService.getSingleResult(analyticsService.countMembers());
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
        return responseService.getSingleResult(analyticsService.countMembersByPeriod(startDate, endDate));
    }


    @GetMapping(value = "/wishlistboards")
    public CommonResult getWishlistBoardAnalytics(
            @RequestParam(value = "startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Optional<LocalDate> startDate,
            @RequestParam(value = "endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Optional<LocalDate> endDate
    ) {
        return responseService.getSingleResult(analyticsService.analyzeWishlistBoardByPeriod(startDate, endDate));
    }


    @GetMapping(value = "/reviews")
    public CommonResult getReviewAnalytics(
            @RequestParam(value = "startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Optional<LocalDate> startDate,
            @RequestParam(value = "endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Optional<LocalDate> endDate
    ) {
        return responseService.getSingleResult(analyticsService.analyzeReviewByPeriod(startDate, endDate));
    }



    @GetMapping(value = "/accumulated-reviews/count")
    public CommonResult getAccumulatedReviewsCount(
            @RequestParam(value = "startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Optional<LocalDate> startDate,
            @RequestParam(value = "endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Optional<LocalDate> endDate
    ) {
        return responseService.getListResult(analyticsService.countAccumulatedReviewsByPeriod(startDate, endDate));
    }

}
