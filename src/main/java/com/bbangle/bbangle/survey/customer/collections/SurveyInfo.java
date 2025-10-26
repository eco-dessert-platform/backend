package com.bbangle.bbangle.survey.customer.collections;

import lombok.Builder;

@Builder
public record SurveyInfo(
    DietLimitations dietLimitations,
    UnmatchedIngredients unmatchedIngredients,
    HealthConcerns healthConcerns,
    IsVegetarians isVegetarians
) {

}
