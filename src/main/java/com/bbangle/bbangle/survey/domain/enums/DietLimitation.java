package com.bbangle.bbangle.survey.domain.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DietLimitation {
    LACTOSE_INTOLERANCE("유당 불내증"),
    GLUTEN_INTOLERANCE("글루틴 불내증"),
    DIABETES("당뇨"),
    NOT_APPLICABLE("해당없음");

    private final String description;

}
