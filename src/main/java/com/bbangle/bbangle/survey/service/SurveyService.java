package com.bbangle.bbangle.survey.service;

import com.bbangle.bbangle.survey.collections.SurveyInfo;
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
        UnmatchedIngredientsInfo unmatchedInfo = surveyRequest.hateFoods().getInfo();

        FoodSurvey foodSurvey = FoodSurvey.builder()
            .memberId(memberId)
            .dietLimitationInfo(dietInfo)
            .healthConcernInfo(healthInfo)
            .isVegetarianInfo(vegetarianInfo)
            .unmatchedIngredientsInfo(unmatchedInfo)
            .build();

        surveyRepository.save(foodSurvey);
    }

}
