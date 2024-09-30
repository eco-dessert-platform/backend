package com.bbangle.bbangle.survey.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    @Embedded
    private DietLimitationInfo dietLimitationInfo;

    @Embedded
    private HealthConcernInfo healthConcernInfo;

    @Embedded
    private IsVegetarianInfo isVegetarianInfo;

    @Embedded
    private UnmatchedIngredientsInfo unmatchedIngredientsInfo;

    public void updateInfo(
        DietLimitationInfo dietInfo,
        HealthConcernInfo healthInfo,
        IsVegetarianInfo vegetarianInfo,
        UnmatchedIngredientsInfo unmatchedInfo
    ) {
        this.dietLimitationInfo = dietInfo;
        this.healthConcernInfo = healthInfo;
        this.isVegetarianInfo = vegetarianInfo;
        this.unmatchedIngredientsInfo = unmatchedInfo;
    }

}
