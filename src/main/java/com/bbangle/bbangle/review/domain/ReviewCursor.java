package com.bbangle.bbangle.review.domain;

import lombok.Builder;

@Builder
public record ReviewCursor(
    Long nextCursor,
    Long lastCursor,
    Long reviewId
) {
}
