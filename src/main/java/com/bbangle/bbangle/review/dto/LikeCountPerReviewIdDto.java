package com.bbangle.bbangle.review.dto;

import com.querydsl.core.annotations.QueryProjection;

public record LikeCountPerReviewIdDto(
        Long reviewId,
        Long likeCount
) {
    @QueryProjection
    public LikeCountPerReviewIdDto {
    }
}
