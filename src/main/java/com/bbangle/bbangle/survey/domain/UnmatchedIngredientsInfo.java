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
public class UnmatchedIngredientsInfo {
    private Boolean flour;
    private Boolean whiteWheat;
    private Boolean rice;
    private Boolean bean;
    private Boolean milk;
    private Boolean soyMilk;
    private Boolean sugar;
    private Boolean egg;
    private Boolean peanut;
    private Boolean walnuts;
    private Boolean pineNuts;
    private Boolean peach;
    private Boolean tomato;
    private Boolean notApplicable;
}
