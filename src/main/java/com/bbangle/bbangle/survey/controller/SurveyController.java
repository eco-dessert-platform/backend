package com.bbangle.bbangle.survey.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.survey.dto.request.RecommendationSurveyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/surveys")
public class SurveyController {

    @PostMapping("/recommendation")
    public CommonResult survey(
        @AuthenticationPrincipal Long memberId,
        @RequestBody
        RecommendationSurveyRequest surveyRequest
    ){
        return null;
    }
}
