package com.bbangle.bbangle.board.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MainCategory {

    BREAD("빵"),
    SNACK("과자/간식");

    private final String description;
}
