package com.bbangle.bbangle.survey.customer.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.survey.customer.collections.DietLimitations;
import com.bbangle.bbangle.survey.customer.collections.HealthConcerns;
import com.bbangle.bbangle.survey.customer.collections.IsVegetarians;
import com.bbangle.bbangle.survey.customer.collections.SurveyInfo;
import com.bbangle.bbangle.survey.customer.collections.UnmatchedIngredients;
import com.bbangle.bbangle.survey.customer.dto.request.RecommendationSurveyDto;
import com.bbangle.bbangle.survey.customer.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
        RecommendationSurveyDto surveyRequest
    ) {
        surveyService.recordSurvey(
            memberId,
            SurveyInfo.builder()
                .dietLimitations(new DietLimitations(surveyRequest.dietLimitation()))
                .unmatchedIngredients(
                    new UnmatchedIngredients(surveyRequest.unmatchedIngredientList()))
                .healthConcerns(new HealthConcerns(surveyRequest.healthConcerns()))
                .isVegetarians(new IsVegetarians(surveyRequest.isVegetarians()))
                .build());
        return responseService.getSuccessResult();
    }

    @PutMapping("/recommendation")
    public CommonResult surveyFix(
        @AuthenticationPrincipal
        Long memberId,
        @RequestBody
        RecommendationSurveyDto surveyRequest
    ) {
        surveyService.updateSurvey(
            memberId,
            SurveyInfo.builder()
                .dietLimitations(new DietLimitations(surveyRequest.dietLimitation()))
                .unmatchedIngredients(
                    new UnmatchedIngredients(surveyRequest.unmatchedIngredientList()))
                .healthConcerns(new HealthConcerns(surveyRequest.healthConcerns()))
                .isVegetarians(new IsVegetarians(surveyRequest.isVegetarians()))
                .build());
        return responseService.getSuccessResult();
    }

    @GetMapping("/recommendation")
    public SingleResult<RecommendationSurveyDto> getSurvey(
        @AuthenticationPrincipal
        Long memberId
    ) {
        RecommendationSurveyDto response = surveyService.getSurveyInfo(memberId);
        return responseService.getSingleResult(response);
    }
}
