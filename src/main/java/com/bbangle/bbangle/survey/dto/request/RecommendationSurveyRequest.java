package com.bbangle.bbangle.survey.dto.request;

import com.bbangle.bbangle.survey.enums.DietLimitation;
import com.bbangle.bbangle.survey.enums.HealthConcern;
import com.bbangle.bbangle.survey.enums.IsVegetarian;
import com.bbangle.bbangle.survey.enums.UnmatchedIngredients;
import java.util.List;

public record RecommendationSurveyRequest(
    List<DietLimitation> dietLimitation,
    List<HealthConcern> healthConcerns,
    List<UnmatchedIngredients> hateFoodList,
    List<IsVegetarian> isVegetarians
) {

}
