package com.bbangle.bbangle.survey.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum HealthConcern {
    PIMPLES("여드름"),
    BODY_FAT("체지방"),
    CHOLESTEROL("콜레스테롤"),
    DIGESTIVE_DISORDER("소화불량"),
    NOT_APPLICABLE("해당 없음");

    private final String description;

}
