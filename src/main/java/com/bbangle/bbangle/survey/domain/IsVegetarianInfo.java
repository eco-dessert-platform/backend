package com.bbangle.bbangle.survey.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Embeddable
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IsVegetarianInfo {
    private Boolean fruit;
    private Boolean animalIngredient;
    private Boolean diary;
    private Boolean egg;
    private Boolean seaFood;
    private Boolean meat;
}
