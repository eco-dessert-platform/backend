package com.bbangle.bbangle.survey.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UnmatchedIngredient {
    FLOUR("밀가루"),
    WHOLE_WHEAT("통밀"),
    RICE("쌀"),
    BEAN("콩"),
    MILK("우유"),
    SOY_MILK("두유"),
    SUGAR("설탕"),
    EGG("계란"),
    PEANUT("땅콩"),
    WALNUTS("호두"),
    PINE_NUTS("잣"),
    PEACH("복숭아"),
    TOMATO("토마토"),
    NOT_APPLICABLE("해당없음");

    private final String description;
}
