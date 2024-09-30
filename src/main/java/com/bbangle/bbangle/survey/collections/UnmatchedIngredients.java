package com.bbangle.bbangle.survey.collections;

import com.bbangle.bbangle.survey.domain.UnmatchedIngredientsInfo;
import com.bbangle.bbangle.survey.enums.UnmatchedIngredient;
import java.util.ArrayList;
import java.util.List;

public record UnmatchedIngredients(
    List<UnmatchedIngredient> hateFoods
) {

    public static List<UnmatchedIngredient> of(UnmatchedIngredientsInfo unmatchedIngredientsInfo) {
        List<UnmatchedIngredient> unmatchedIngredientList = new ArrayList<>();
        if(unmatchedIngredientsInfo.getFlour()){
            unmatchedIngredientList.add(UnmatchedIngredient.FLOUR);
        }
        if(unmatchedIngredientsInfo.getWhiteWheat()){
            unmatchedIngredientList.add(UnmatchedIngredient.WHOLE_WHEAT);
        }
        if(unmatchedIngredientsInfo.getRice()){
            unmatchedIngredientList.add(UnmatchedIngredient.RICE);
        }
        if(unmatchedIngredientsInfo.getBean()){
            unmatchedIngredientList.add(UnmatchedIngredient.BEAN);
        }
        if(unmatchedIngredientsInfo.getMilk()){
            unmatchedIngredientList.add(UnmatchedIngredient.MILK);
        }
        if(unmatchedIngredientsInfo.getSoyMilk()){
            unmatchedIngredientList.add(UnmatchedIngredient.SOY_MILK);
        }
        if(unmatchedIngredientsInfo.getSugar()){
            unmatchedIngredientList.add(UnmatchedIngredient.SUGAR);
        }
        if(unmatchedIngredientsInfo.getEgg()){
            unmatchedIngredientList.add(UnmatchedIngredient.EGG);
        }
        if(unmatchedIngredientsInfo.getPeanut()){
            unmatchedIngredientList.add(UnmatchedIngredient.PEANUT);
        }
        if(unmatchedIngredientsInfo.getWalnuts()){
            unmatchedIngredientList.add(UnmatchedIngredient.WALNUTS);
        }
        if(unmatchedIngredientsInfo.getPineNuts()){
            unmatchedIngredientList.add(UnmatchedIngredient.PINE_NUTS);
        }
        if(unmatchedIngredientsInfo.getPeach()){
            unmatchedIngredientList.add(UnmatchedIngredient.PEACH);
        }
        if(unmatchedIngredientsInfo.getTomato()){
            unmatchedIngredientList.add(UnmatchedIngredient.TOMATO);
        }
        if(unmatchedIngredientsInfo.getNotApplicable()){
            unmatchedIngredientList.add(UnmatchedIngredient.NOT_APPLICABLE);
        }

        return unmatchedIngredientList;
    }

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

        for (UnmatchedIngredient limit : hateFoods) {
            if (limit == UnmatchedIngredient.FLOUR) {
                flour = true;
            }

            if (limit == UnmatchedIngredient.WHOLE_WHEAT) {
                whiteWheat = true;
            }

            if (limit == UnmatchedIngredient.RICE) {
                rice = true;
            }

            if (limit == UnmatchedIngredient.BEAN) {
                bean = true;
            }

            if (limit == UnmatchedIngredient.MILK) {
                milk = true;
            }

            if (limit == UnmatchedIngredient.SOY_MILK) {
                soyMilk = true;
            }

            if (limit == UnmatchedIngredient.SUGAR) {
                sugar = true;
            }

            if (limit == UnmatchedIngredient.EGG) {
                egg = true;
            }

            if (limit == UnmatchedIngredient.PEANUT) {
                peanut = true;
            }

            if (limit == UnmatchedIngredient.WALNUTS) {
                walnuts = true;
            }

            if (limit == UnmatchedIngredient.PINE_NUTS) {
                pineNuts = true;
            }

            if (limit == UnmatchedIngredient.PEACH) {
                peach = true;
            }

            if (limit == UnmatchedIngredient.TOMATO) {
                tomato = true;
            }

            if (limit == UnmatchedIngredient.NOT_APPLICABLE) {
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
