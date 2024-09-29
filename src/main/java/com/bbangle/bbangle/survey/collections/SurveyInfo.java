package com.bbangle.bbangle.survey.collections;

import lombok.Builder;

@Builder
public record SurveyInfo (
    DietLimitations dietLimitations,
    HateFoods hateFoods,
    HealthConcerns healthConcerns,
    IsVegetarians isVegetarians
){

}
