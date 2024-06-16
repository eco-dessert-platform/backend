package com.bbangle.bbangle.config.ranking;

import lombok.Builder;

@Builder
public record BoardWishCount(
    Long boardId,
    int count
) {

}
