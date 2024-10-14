package com.bbangle.bbangle.survey.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum IsVegetarian {
    FRUIT("과일"),
    ANIMAL_INGREDIENT("동물성 재료"),
    DIARY("유제품"),
    ANIMAL_EGG("동물의 알"),
    SEA_FOOD("해산물"),
    MEAT("육고기");

    private final String description;
}
