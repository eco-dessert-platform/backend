package com.bbangle.bbangle.survey.collections;

import com.bbangle.bbangle.survey.domain.IsVegetarianInfo;
import com.bbangle.bbangle.survey.enums.IsVegetarian;
import java.util.ArrayList;
import java.util.List;

public record IsVegetarians(
    List<IsVegetarian> isVegetarians
) {

    public static List<IsVegetarian> of(IsVegetarianInfo isVegetarianInfo) {
        List<IsVegetarian> isVegetarianList = new ArrayList<>();
        if (isVegetarianInfo.getMeat()) {
            isVegetarianList.add(IsVegetarian.MEAT);
        }
        if (isVegetarianInfo.getEgg()) {
            isVegetarianList.add(IsVegetarian.EGG);
        }
        if (isVegetarianInfo.getFruit()) {
            isVegetarianList.add(IsVegetarian.FRUIT);
        }
        if (isVegetarianInfo.getDiary()) {
            isVegetarianList.add(IsVegetarian.DIARY);
        }
        if (isVegetarianInfo.getAnimalIngredient()) {
            isVegetarianList.add(IsVegetarian.ANIMAL_INGREDIENT);
        }
        if (isVegetarianInfo.getSeaFood()) {
            isVegetarianList.add(IsVegetarian.SEA_FOOD);
        }
        return isVegetarianList;
    }

    public IsVegetarianInfo getInfo() {
        boolean fruit = false;
        boolean animalIngredient = false;
        boolean diary = false;
        boolean egg = false;
        boolean seaFood = false;
        boolean meat = false;

        for (IsVegetarian isVegetarian : isVegetarians) {
            if (isVegetarian == IsVegetarian.FRUIT) {
                fruit = true;
            }

            if (isVegetarian == IsVegetarian.ANIMAL_INGREDIENT) {
                animalIngredient = true;
            }

            if (isVegetarian == IsVegetarian.DIARY) {
                diary = true;
            }

            if (isVegetarian == IsVegetarian.EGG) {
                egg = true;
            }

            if (isVegetarian == IsVegetarian.SEA_FOOD) {
                seaFood = true;
            }

            if (isVegetarian == IsVegetarian.MEAT) {
                meat = true;
            }
        }

        return IsVegetarianInfo.builder()
            .fruit(fruit)
            .animalIngredient(animalIngredient)
            .diary(diary)
            .egg(egg)
            .seaFood(seaFood)
            .meat(meat)
            .build();
    }

}
