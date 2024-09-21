package com.bbangle.bbangle.boardstatistic.ranking;

import lombok.Builder;

@Builder
public record BoardWishCount(
    Long boardId,
    int count
) {

}
