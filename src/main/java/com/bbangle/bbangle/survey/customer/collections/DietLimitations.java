package com.bbangle.bbangle.survey.customer.collections;

import com.bbangle.bbangle.survey.domain.DietLimitationInfo;
import com.bbangle.bbangle.survey.domain.enums.DietLimitation;
import java.util.ArrayList;
import java.util.List;

public record DietLimitations(
    List<DietLimitation> dietLimitations
) {

    public static List<DietLimitation> of(DietLimitationInfo dietLimitationInfo) {
        List<DietLimitation> dietLimitationList = new ArrayList<>();
        if(dietLimitationInfo.getDiabetes()){
            dietLimitationList.add(DietLimitation.DIABETES);
        }
        if(dietLimitationInfo.getLactoseIntolerance()){
            dietLimitationList.add(DietLimitation.LACTOSE_INTOLERANCE);
        }
        if(dietLimitationInfo.getNotApplicable()){
            dietLimitationList.add(DietLimitation.NOT_APPLICABLE);
        }
        if(dietLimitationInfo.getGlutenIntolerance()){
            dietLimitationList.add(DietLimitation.GLUTEN_INTOLERANCE);
        }
        return dietLimitationList;
    }

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
