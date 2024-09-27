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
public class UnmatchedIngredientsInfo {
    @Column(columnDefinition = "tinyint", name = "q3_flour")
    private Boolean flour;
    @Column(columnDefinition = "tinyint", name = "q3_white_wheat")
    private Boolean whiteWheat;
    @Column(columnDefinition = "tinyint", name = "q3_rice")
    private Boolean rice;
    @Column(columnDefinition = "tinyint", name = "q3_bean")
    private Boolean bean;
    @Column(columnDefinition = "tinyint", name = "q3_milk")
    private Boolean milk;
    @Column(columnDefinition = "tinyint", name = "q3_soy_milk")
    private Boolean soyMilk;
    @Column(columnDefinition = "tinyint", name = "q3_sugar")
    private Boolean sugar;
    @Column(columnDefinition = "tinyint", name = "q3_egg")
    private Boolean egg;
    @Column(columnDefinition = "tinyint", name = "q3_peanut")
    private Boolean peanut;
    @Column(columnDefinition = "tinyint", name = "q3_walnuts")
    private Boolean walnuts;
    @Column(columnDefinition = "tinyint", name = "q3_pine_nuts")
    private Boolean pineNuts;
    @Column(columnDefinition = "tinyint", name = "q3_peach")
    private Boolean peach;
    @Column(columnDefinition = "tinyint", name = "q3_tomato")
    private Boolean tomato;
    @Column(columnDefinition = "tinyint", name = "q_3_not_applicable")
    private Boolean notApplicable;
}
