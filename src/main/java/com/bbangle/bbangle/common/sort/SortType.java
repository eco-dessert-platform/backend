package com.bbangle.bbangle.common.sort;

import lombok.Getter;

@Getter
public enum SortType {
    RECENT("recent"),
    LOW_PRICE("low-price"),
    POPULAR("popular"),
    RECOMMEND("recommend");

    private static final String ERROR_WORD = "올바르지 않은 분류 값입니다. 다시 선택해주세요";
    private final String value;

    SortType(String value) {
        this.value = value;
    }
    
}
