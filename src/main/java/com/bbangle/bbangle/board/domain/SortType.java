package com.bbangle.bbangle.board.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SortType {

    LATEST("최신순"),
    OLDEST("오래된 순"),
    NAME("상품명 순");

    private final String description;
}
