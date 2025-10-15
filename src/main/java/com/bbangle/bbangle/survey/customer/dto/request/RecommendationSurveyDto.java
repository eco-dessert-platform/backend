package com.bbangle.bbangle.survey.customer.dto.request;

import com.bbangle.bbangle.survey.customer.collections.DietLimitations;
import com.bbangle.bbangle.survey.customer.collections.UnmatchedIngredients;
import com.bbangle.bbangle.survey.customer.collections.HealthConcerns;
import com.bbangle.bbangle.survey.customer.collections.IsVegetarians;
import com.bbangle.bbangle.survey.domain.FoodSurvey;
import com.bbangle.bbangle.survey.domain.enums.DietLimitation;
import com.bbangle.bbangle.survey.domain.enums.HealthConcern;
import com.bbangle.bbangle.survey.domain.enums.IsVegetarian;
import com.bbangle.bbangle.survey.domain.enums.UnmatchedIngredient;
import java.util.List;

public record RecommendationSurveyDto(
    List<DietLimitation> dietLimitation,
    List<HealthConcern> healthConcerns,
    List<UnmatchedIngredient> unmatchedIngredientList,
    List<IsVegetarian> isVegetarians
) {

    public static RecommendationSurveyDto of(FoodSurvey existSurvey) {
        List<DietLimitation> dietLimitations = DietLimitations.of(existSurvey.getDietLimitationInfo());
        List<HealthConcern> healthConcerns = HealthConcerns.of(existSurvey.getHealthConcernInfo());
        List<UnmatchedIngredient> hateFoodList = UnmatchedIngredients.of(existSurvey.getUnmatchedIngredientsInfo());
        List<IsVegetarian> isVegetarians = IsVegetarians.of(existSurvey.getIsVegetarianInfo());
        return new RecommendationSurveyDto(dietLimitations, healthConcerns, hateFoodList, isVegetarians);
    }

}
