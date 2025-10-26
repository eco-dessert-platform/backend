package com.bbangle.bbangle.survey.customer.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.survey.customer.collections.SurveyInfo;
import com.bbangle.bbangle.survey.customer.dto.request.RecommendationSurveyDto;
import com.bbangle.bbangle.survey.domain.DietLimitationInfo;
import com.bbangle.bbangle.survey.domain.FoodSurvey;
import com.bbangle.bbangle.survey.domain.HealthConcernInfo;
import com.bbangle.bbangle.survey.domain.IsVegetarianInfo;
import com.bbangle.bbangle.survey.domain.UnmatchedIngredientsInfo;
import com.bbangle.bbangle.survey.repository.SurveyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final SurveyRepository surveyRepository;

    @Transactional
    public void recordSurvey(Long memberId, SurveyInfo surveyRequest) {
        DietLimitationInfo dietInfo = surveyRequest.dietLimitations().getInfo();
        HealthConcernInfo healthInfo = surveyRequest.healthConcerns().getInfo();
        IsVegetarianInfo vegetarianInfo = surveyRequest.isVegetarians().getInfo();
        UnmatchedIngredientsInfo unmatchedInfo = surveyRequest.unmatchedIngredients().getInfo();

        FoodSurvey foodSurvey = FoodSurvey.builder()
            .memberId(memberId)
            .dietLimitationInfo(dietInfo)
            .healthConcernInfo(healthInfo)
            .isVegetarianInfo(vegetarianInfo)
            .unmatchedIngredientsInfo(unmatchedInfo)
            .build();

        surveyRepository.save(foodSurvey);
    }

    @Transactional
    public void updateSurvey(Long memberId, SurveyInfo surveyRequest) {
        FoodSurvey existSurvey = surveyRepository.findByMemberId(memberId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.SURVEY_NOT_FOUND));
        DietLimitationInfo dietInfo = surveyRequest.dietLimitations().getInfo();
        HealthConcernInfo healthInfo = surveyRequest.healthConcerns().getInfo();
        IsVegetarianInfo vegetarianInfo = surveyRequest.isVegetarians().getInfo();
        UnmatchedIngredientsInfo unmatchedInfo = surveyRequest.unmatchedIngredients().getInfo();

        existSurvey.updateInfo(dietInfo, healthInfo, vegetarianInfo, unmatchedInfo);
    }

    public RecommendationSurveyDto getSurveyInfo(Long memberId) {
        FoodSurvey existSurvey = surveyRepository.findByMemberId(memberId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.SURVEY_NOT_FOUND));

        return RecommendationSurveyDto.of(existSurvey);
    }

}
