package com.bbangle.bbangle.boardstatistic.customer.ranking;

import lombok.Builder;

@Builder
public record BoardWishCount(
    Long boardId,
    int count
) {

}
