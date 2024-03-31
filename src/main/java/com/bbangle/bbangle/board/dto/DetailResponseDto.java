package com.bbangle.bbangle.board.dto;

import com.querydsl.core.annotations.QueryProjection;

public record DetailResponseDto(
        Long id,
        Integer imgIndex,
        String url
) {
    @QueryProjection
    public DetailResponseDto(Long id, Integer imgIndex, String url){
        this.id = id;
        this.imgIndex = imgIndex;
        this.url = url;
    }
}
