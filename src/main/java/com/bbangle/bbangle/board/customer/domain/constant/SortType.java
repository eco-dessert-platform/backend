package com.bbangle.bbangle.board.customer.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SortType {
    RECENT("최신순"),
    LOW_PRICE("낮은 가격순"),
    HIGH_PRICE("높은 가격순"),
    RECOMMEND("추천순"),
    MOST_WISHED("찜 많은 순"),
    MOST_REVIEWED("리뷰 많은 순"),
    HIGHEST_RATED("만족도 순");

    private final String description;
}
