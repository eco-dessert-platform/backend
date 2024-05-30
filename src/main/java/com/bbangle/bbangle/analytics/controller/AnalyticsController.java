package com.bbangle.bbangle.analytics.controller;


import com.bbangle.bbangle.analytics.dto.*;
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
        AnalyticsMembersCountResponseDto response = AnalyticsMembersCountResponseDto.builder()
                .count(analyticsService.countAllMembers())
                .build();

        return responseService.getSingleResult(response);
    }


    @GetMapping(value = "/new-members/count")
    public CommonResult getNewMembersCount(
            @RequestParam(value = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDate,
            @RequestParam(value = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> endDate
    ) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        List<AnalyticsCountWithDateResponseDto> response = analyticsService.countMembersByPeriod(startLocalDate, endLocalDate);
        return responseService.getListResult(response);
    }


    @GetMapping(value = "/ratio/wishlist-usage")
    public CommonResult getWishlistUsageRatio(
            @RequestParam(value = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDate,
            @RequestParam(value = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> endDate
    ) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        List<AnalyticsRatioWithDateResponseDto> response = analyticsService.calculateWishlistUsingRatio(startLocalDate, endLocalDate);
        return responseService.getListResult(response);
    }


    @GetMapping(value = "/wishlist/boards/ranking")
    public CommonResult getWishlistBoardRanking() {
        List<AnalyticsWishlistBoardRankingResponseDto> response = analyticsService.getWishlistBoardRanking();
        return responseService.getListResult(response);
    }


    @GetMapping(value = "/wishlist/boards/count")
    public CommonResult getWishlistUsageCount(
            @RequestParam(value = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDate,
            @RequestParam(value = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> endDate
    ) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        List<AnalyticsCountWithDateResponseDto> response = analyticsService.countWishlistBoardByPeriod(startLocalDate, endLocalDate);
        return responseService.getListResult(response);
    }


    @GetMapping(value = "/ratio/review-usage")
    public CommonResult getReviewUsageRatio(
            @RequestParam(value = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDate,
            @RequestParam(value = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> endDate
    ) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        List<AnalyticsRatioWithDateResponseDto> response = analyticsService.calculateReviewUsingRatio(startLocalDate, endLocalDate);
        return responseService.getListResult(response);
    }


    @GetMapping(value = "/reviews/count")
    public CommonResult getReviewUsageCount(
            @RequestParam(value = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> startDate,
            @RequestParam(value = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> endDate
    ) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        List<AnalyticsCountWithDateResponseDto> response = analyticsService.countReviewByPeriod(startLocalDate, endLocalDate);
        return responseService.getListResult(response);
    }

}
