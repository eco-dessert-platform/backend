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
public class DietLimitationInfo {
    private Boolean lactoseIntolerance;
    private Boolean glutenIntolerance;
    private Boolean diabetes;
    private Boolean notApplicable;

}
