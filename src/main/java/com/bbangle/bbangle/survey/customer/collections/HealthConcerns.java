package com.bbangle.bbangle.survey.customer.collections;

import com.bbangle.bbangle.survey.domain.HealthConcernInfo;
import com.bbangle.bbangle.survey.domain.enums.HealthConcern;
import java.util.ArrayList;
import java.util.List;

public record HealthConcerns(
    List<HealthConcern> healthConcerns
) {

    public static List<HealthConcern> of(HealthConcernInfo healthConcernInfo) {
        List<HealthConcern> healthConcernList = new ArrayList<>();
        if(healthConcernInfo.getPimples()){
            healthConcernList.add(HealthConcern.PIMPLES);
        }
        if(healthConcernInfo.getBodyFat()){
            healthConcernList.add(HealthConcern.BODY_FAT);
        }
        if(healthConcernInfo.getCholesterol()){
            healthConcernList.add(HealthConcern.CHOLESTEROL);
        }
        if(healthConcernInfo.getNotApplicable()){
            healthConcernList.add(HealthConcern.NOT_APPLICABLE);
        }
        if(healthConcernInfo.getDigestiveDisorder()){
            healthConcernList.add(HealthConcern.DIGESTIVE_DISORDER);
        }
        return healthConcernList;
    }

    public HealthConcernInfo getInfo() {
        boolean pimples = false;
        boolean bodyFat = false;
        boolean cholesterol = false;
        boolean digestiveDisorder = false;
        boolean notApplicable = false;

        for(HealthConcern healthConcern : healthConcerns) {
            if(healthConcern == HealthConcern.PIMPLES){
                pimples = true;
            }

            if(healthConcern == HealthConcern.BODY_FAT){
                bodyFat = true;
            }

            if(healthConcern == HealthConcern.CHOLESTEROL){
                cholesterol = true;
            }

            if(healthConcern == HealthConcern.DIGESTIVE_DISORDER){
                digestiveDisorder = true;
            }

            if(healthConcern == HealthConcern.NOT_APPLICABLE){
                notApplicable = true;
            }
        }
        return HealthConcernInfo.builder()
            .pimples(pimples)
            .bodyFat(bodyFat)
            .cholesterol(cholesterol)
            .digestiveDisorder(digestiveDisorder)
            .notApplicable(notApplicable)
            .build();
    }

}
