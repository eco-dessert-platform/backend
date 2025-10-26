package com.bbangle.bbangle.preference.domain;

import com.bbangle.bbangle.board.repository.dao.TagsDao;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PreferenceType {
    NONE("선택안함", PreferenceType::calculateNoneScore),
    DIET("다이어트", PreferenceType::calculateDietOrMuscleGrowScore),
    MUSCLE_GROW("근육 증가", PreferenceType::calculateDietOrMuscleGrowScore),
    CONSTITUTION("체질 개선", PreferenceType::calculateAllergyScore),
    VEGAN("비건", PreferenceType::calculateVeganScore),
    DIET_MUSCLE_GROW("다이어트 + 근육 증가", tags -> calculateDietOrMuscleGrowScore(tags) * 2),
    DIET_CONSTITUTION("다이어트 + 체질 개선",
        tags -> calculateDietOrMuscleGrowScore(tags) + calculateAllergyScore(tags)),
    DIET_VEGAN("다이어트 + 비건",
        tags -> calculateDietOrMuscleGrowScore(tags) + calculateVeganScore(tags)),
    MUSCLE_GROW_CONSTITUTION("근육 증가 + 체질 개선",
        tags -> calculateDietOrMuscleGrowScore(tags) + calculateAllergyScore(tags)),
    MUSCLE_GROW_VEGAN("근육 증가 + 비건",
        tags -> calculateDietOrMuscleGrowScore(tags) + calculateVeganScore(tags)),
    CONSTITUTION_VEGAN("체질 개선 + 비건",
        tags -> calculateVeganScore(tags) + calculateAllergyScore(tags));

    private final String description;
    private final Function<TagsDao, Integer> function;

    public int getCalculatedScore(TagsDao tags) {
        return function.apply(tags);
    }

    private static int calculateNoneScore(TagsDao tags) {
        return 0;
    }

    private static int calculateDietOrMuscleGrowScore(TagsDao tags) {
        int score = 0;
        if (tags.highProteinTag()) {
            score++;
        }
        if (tags.sugarFreeTag()) {
            score++;
        }
        if (tags.glutenFreeTag()) {
            score++;
        }
        return score;
    }

    private static int calculateAllergyScore(TagsDao tags) {
        int score = 0;
        if (tags.glutenFreeTag()) {
            score++;
        }
        return score;
    }


    private static int calculateVeganScore(TagsDao tags) {
        int score = 0;
        if (tags.veganTag()) {
            score++;
        }
        if (tags.highProteinTag()) {
            score++;
        }
        if (tags.sugarFreeTag()) {
            score++;
        }
        if (tags.glutenFreeTag()) {
            score++;
        }
        return score;
    }

}
