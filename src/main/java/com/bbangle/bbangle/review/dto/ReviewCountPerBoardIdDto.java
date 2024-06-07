package com.bbangle.bbangle.review.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

@Builder
public record ReviewCountPerBoardIdDto(
        Long boardId,
        Long reviewCount
) {
    @QueryProjection
    public ReviewCountPerBoardIdDto {
    }
}
