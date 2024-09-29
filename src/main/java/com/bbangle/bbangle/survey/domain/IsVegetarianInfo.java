package com.bbangle.bbangle.survey.domain;

import jakarta.persistence.Column;
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
    @Column(columnDefinition = "tinyint", name = "q4_fruit")
    private Boolean fruit;
    @Column(columnDefinition = "tinyint", name = "q4_animal_ingredient")
    private Boolean animalIngredient;
    @Column(columnDefinition = "tinyint", name = "q4_diary")
    private Boolean diary;
    @Column(columnDefinition = "tinyint", name = "q4_egg")
    private Boolean egg;
    @Column(columnDefinition = "tinyint", name = "q4_sea_food")
    private Boolean seaFood;
    @Column(columnDefinition = "tinyint", name = "q4_meat")
    private Boolean meat;
}
