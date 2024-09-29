package com.bbangle.bbangle.survey.collections;

import com.bbangle.bbangle.survey.domain.DietLimitationInfo;
import com.bbangle.bbangle.survey.enums.DietLimitation;
import java.util.List;

public record DietLimitations(
    List<DietLimitation> dietLimitations
) {

    public DietLimitationInfo getInfo() {
        boolean lactoseIntolerance = false;
        boolean glutenIntolerance = false;
        boolean diabetes = false;
        boolean notApplicable = false;

        for (DietLimitation limit : dietLimitations) {
            if (limit == DietLimitation.DIABETES) {
                diabetes = true;
            }

            if (limit == DietLimitation.GLUTEN_INTOLERANCE) {
                glutenIntolerance = true;
            }

            if (limit == DietLimitation.NOT_APPLICABLE) {
                notApplicable = true;
            }

            if (limit == DietLimitation.LACTOSE_INTOLERANCE) {
                lactoseIntolerance = true;
            }
        }

        return DietLimitationInfo.builder()
            .lactoseIntolerance(lactoseIntolerance)
            .glutenIntolerance(glutenIntolerance)
            .diabetes(diabetes)
            .notApplicable(notApplicable)
            .build();
    }

}
