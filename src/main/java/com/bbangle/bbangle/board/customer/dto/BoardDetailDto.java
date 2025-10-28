package com.bbangle.bbangle.board.customer.dto;

import com.querydsl.core.annotations.QueryProjection;

public record BoardDetailDto(
    String imageUrl
) {

    @QueryProjection
    public BoardDetailDto(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
