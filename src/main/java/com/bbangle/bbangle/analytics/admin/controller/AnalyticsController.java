package com.bbangle.bbangle.analytics.admin.controller;

import com.bbangle.bbangle.analytics.admin.dto.AnalyticsCreatedWithinPeriodResponseDto;
import com.bbangle.bbangle.analytics.admin.dto.AnalyticsCumulationResponseDto;
import com.bbangle.bbangle.analytics.admin.dto.AnalyticsMembersCountResponseDto;
import com.bbangle.bbangle.analytics.admin.service.AnalyticsService;
import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Analytics", description = "관리자 통계 API")
@RestController
@RequestMapping("api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final ResponseService responseService;
    private final AnalyticsService analyticsService;

    @Operation(summary = "전체 회원 수 조회")
    @GetMapping(value = "/members/count")
    public SingleResult<AnalyticsMembersCountResponseDto> getMembersCount() {
        return responseService.getSingleResult(analyticsService.countMembers());
    }

    @Operation(summary = "기간 내 가입한 회원 수 조회")
    @GetMapping(value = "/new-members/count")
    public SingleResult<AnalyticsMembersCountResponseDto> getNewMembersCount(
            @RequestParam(value = "startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "조회 시작 날짜 (yyyy-MM-dd)", example = "2024-01-01")
            Optional<LocalDate> startDate,
            @RequestParam(value = "endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "조회 종료 날짜 (yyyy-MM-dd)", example = "2024-12-31")
            Optional<LocalDate> endDate
    ) {
        return responseService.getSingleResult(analyticsService.countMembersByPeriod(startDate, endDate));
    }

    @Operation(summary = "기간 내 날짜 별 생성된 찜 통계 조회")
    @GetMapping(value = "/wishlistboards")
    public SingleResult<AnalyticsCreatedWithinPeriodResponseDto> getWishlistBoardAnalytics(
            @RequestParam(value = "startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "조회 시작 날짜 (yyyy-MM-dd)", example = "2024-01-01")
            Optional<LocalDate> startDate,
            @Parameter(description = "조회 종료 날짜 (yyyy-MM-dd)", example = "2024-12-31")
            @RequestParam(value = "endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            Optional<LocalDate> endDate
    ) {
        return responseService.getSingleResult(analyticsService.analyzeWishlistBoardByPeriod(startDate, endDate));
    }

    @Operation(summary = "기간 내 날짜 별 생성된 리뷰 통계 조회")
    @GetMapping(value = "/reviews")
    public SingleResult<AnalyticsCreatedWithinPeriodResponseDto> getReviewAnalytics(
            @RequestParam(value = "startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "조회 시작 날짜 (yyyy-MM-dd)", example = "2024-01-01")
            Optional<LocalDate> startDate,
            @RequestParam(value = "endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "조회 종료 날짜 (yyyy-MM-dd)", example = "2024-12-31")
            Optional<LocalDate> endDate
    ) {
        return responseService.getSingleResult(analyticsService.analyzeReviewByPeriod(startDate, endDate));
    }

    @Operation(summary = "기간 내 날짜 별 누적된 리뷰 수 조회")
    @GetMapping(value = "/accumulated-reviews/count")
    public ListResult<AnalyticsCumulationResponseDto> getCumulatedReviewsCount(
            @RequestParam(value = "startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "조회 시작 날짜 (yyyy-MM-dd)", example = "2024-01-01")
            Optional<LocalDate> startDate,
            @RequestParam(value = "endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "조회 종료 날짜 (yyyy-MM-dd)", example = "2024-12-31")
            Optional<LocalDate> endDate
    ) {
        return responseService.getListResult(analyticsService.countCumulatedReviewsByPeriod(startDate, endDate));
    }
}
