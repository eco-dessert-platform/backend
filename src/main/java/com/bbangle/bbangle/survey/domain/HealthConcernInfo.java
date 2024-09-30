package com.bbangle.bbangle.survey.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthConcernInfo {
    @Column(columnDefinition = "tinyint", name = "q2_pimples")
    private Boolean pimples;
    @Column(columnDefinition = "tinyint", name = "q2_body_fat")
    private Boolean bodyFat;
    @Column(columnDefinition = "tinyint", name = "q2_cholesterol")
    private Boolean cholesterol;
    @Column(columnDefinition = "tinyint", name = "q2_digestive_disorder")
    private Boolean digestiveDisorder;
    @Column(columnDefinition = "tinyint",  name = "q2_not_applicable")
    private Boolean notApplicable;
}
