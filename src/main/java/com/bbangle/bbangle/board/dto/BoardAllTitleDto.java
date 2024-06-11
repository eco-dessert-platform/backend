package com.bbangle.bbangle.board.dto;

import com.querydsl.core.annotations.QueryProjection;

public record BoardAllTitleDto(
    Long boardId,
    String Title
) {

    @QueryProjection
    public BoardAllTitleDto {

    }
}
