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
public class DietLimitationInfo {
    @Column(columnDefinition = "tinyint", name = "q1_lactose_intolerance")
    private Boolean lactoseIntolerance;
    @Column(columnDefinition = "tinyint", name = "q1_gluten_intolerance")
    private Boolean glutenIntolerance;
    @Column(columnDefinition = "tinyint", name = "q1_diabetes")
    private Boolean diabetes;
    @Column(columnDefinition = "tinyint", name = "q1_not_applicable")
    private Boolean notApplicable;

}
