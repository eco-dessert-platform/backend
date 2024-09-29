package com.bbangle.bbangle.survey.collections;

import com.bbangle.bbangle.survey.domain.HealthConcernInfo;
import com.bbangle.bbangle.survey.enums.HealthConcern;
import java.util.List;

public record HealthConcerns(
    List<HealthConcern> healthConcerns
) {

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
