package com.bbangle.bbangle.survey.collections;

import com.bbangle.bbangle.survey.domain.UnmatchedIngredientsInfo;
import com.bbangle.bbangle.survey.enums.DietLimitation;
import com.bbangle.bbangle.survey.enums.UnmatchedIngredients;
import java.util.List;

public record HateFoods(
    List<UnmatchedIngredients> hateFoods
) {

    public UnmatchedIngredientsInfo getInfo() {
        boolean flour = false;
        boolean whiteWheat = false;
        boolean rice = false;
        boolean bean = false;
        boolean milk = false;
        boolean soyMilk = false;
        boolean sugar = false;
        boolean egg = false;
        boolean peanut = false;
        boolean walnuts = false;
        boolean pineNuts = false;
        boolean peach = false;
        boolean tomato = false;
        boolean notApplicable = false;

        for (UnmatchedIngredients limit : hateFoods) {
            if (limit == UnmatchedIngredients.FLOUR) {
                flour = true;
            }

            if (limit == UnmatchedIngredients.WHOLE_WHEAT) {
                whiteWheat = true;
            }

            if (limit == UnmatchedIngredients.RICE) {
                rice = true;
            }

            if (limit == UnmatchedIngredients.BEAN) {
                bean = true;
            }

            if (limit == UnmatchedIngredients.MILK) {
                milk = true;
            }

            if (limit == UnmatchedIngredients.SOY_MILK) {
                soyMilk = true;
            }

            if (limit == UnmatchedIngredients.SUGAR) {
                sugar = true;
            }

            if (limit == UnmatchedIngredients.EGG) {
                egg = true;
            }

            if (limit == UnmatchedIngredients.PEANUT) {
                peanut = true;
            }

            if (limit == UnmatchedIngredients.WALNUTS) {
                walnuts = true;
            }

            if (limit == UnmatchedIngredients.PINE_NUTS) {
                pineNuts = true;
            }

            if (limit == UnmatchedIngredients.PEACH) {
                peach = true;
            }

            if (limit == UnmatchedIngredients.TOMATO) {
                tomato = true;
            }

            if (limit == UnmatchedIngredients.NOT_APPLICABLE) {
                notApplicable = true;
            }
        }

        return UnmatchedIngredientsInfo.builder()
            .flour(flour)
            .whiteWheat(whiteWheat)
            .rice(rice)
            .bean(bean)
            .milk(milk)
            .soyMilk(soyMilk)
            .sugar(sugar)
            .egg(egg)
            .peanut(peanut)
            .walnuts(walnuts)
            .pineNuts(pineNuts)
            .peach(peach)
            .tomato(tomato)
            .notApplicable(notApplicable)
            .build();
    }

}
