package com.bbangle.bbangle.preference.domain;

public enum PreferenceType {
    DIET("다이어트"),
    MUSCLE_GROW("근육 증가"),
    CONSTITUTION("체질 개선"),
    VEGAN("비건"),
    DIET_MUSCLE_GROW("다이어트 + 근육 증가"),
    DIET_CONSTITUTION("다이어트 + 체질 개선"),
    DIET_VEGAN("다이어트 + 비건"),
    MUSCLE_GROW_CONSTITUTION("근육 증가 + 체질 개선"),
    MUSCLE_GROW_VEGAN("근육 증가 + 비건"),
    CONSTITUTION_VEGAN("체질 개선 + 비건");

    private final String description;

    PreferenceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
