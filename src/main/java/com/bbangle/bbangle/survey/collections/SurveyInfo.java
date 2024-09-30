package com.bbangle.bbangle.survey.collections;

import lombok.Builder;

@Builder
public record SurveyInfo (
    DietLimitations dietLimitations,
    UnmatchedIngredients unmatchedIngredients,
    HealthConcerns healthConcerns,
    IsVegetarians isVegetarians
){

}
