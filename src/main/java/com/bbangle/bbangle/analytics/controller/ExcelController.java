package com.bbangle.bbangle.analytics.controller;

import com.bbangle.bbangle.analytics.dto.AnalyticsMembersCountResponseDto;
import com.bbangle.bbangle.analytics.service.AnalyticsService;
import com.bbangle.bbangle.common.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/analytics/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ResponseService responseService;
    private final AnalyticsService analyticsService;


    @GetMapping(value = "/members/count")
    public ResponseEntity<byte[]> downloadMembersCountExcel() {
        AnalyticsMembersCountResponseDto response = AnalyticsMembersCountResponseDto.builder()
                .count(analyticsService.countAllMembers())
                .build();

        return null;
    }

//    @GetMapping(value = "/new-members/count")
//    @GetMapping(value = "/ratio/wishlist-usage")
//    @GetMapping(value = "/wishlist/boards/ranking")
//    @GetMapping(value = "/wishlist/boards/count")
//    @GetMapping(value = "/ratio/review-usage")
//    @GetMapping(value = "/reviews/count")
}
