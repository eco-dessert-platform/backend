package com.bbangle.bbangle.survey.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.survey.collections.DietLimitations;
import com.bbangle.bbangle.survey.collections.HateFoods;
import com.bbangle.bbangle.survey.collections.HealthConcerns;
import com.bbangle.bbangle.survey.collections.IsVegetarians;
import com.bbangle.bbangle.survey.collections.SurveyInfo;
import com.bbangle.bbangle.survey.dto.request.RecommendationSurveyRequest;
import com.bbangle.bbangle.survey.service.SurveyService;
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

    private final SurveyService surveyService;
    private final ResponseService responseService;

    @PostMapping("/recommendation")
    public CommonResult survey(
        @AuthenticationPrincipal
        Long memberId,
        @RequestBody
        RecommendationSurveyRequest surveyRequest
    ) {
        surveyService.recordSurvey(
            memberId,
            SurveyInfo.builder()
                .dietLimitations(new DietLimitations(surveyRequest.dietLimitation()))
                .hateFoods(new HateFoods(surveyRequest.hateFoodList()))
                .healthConcerns(new HealthConcerns(surveyRequest.healthConcerns()))
                .isVegetarians(new IsVegetarians(surveyRequest.isVegetarians()))
                .build());
        return responseService.getSuccessResult();
    }

}
