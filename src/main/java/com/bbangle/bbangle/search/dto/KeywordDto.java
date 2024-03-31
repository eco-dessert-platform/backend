package com.bbangle.bbangle.search.dto;

import com.querydsl.core.annotations.QueryProjection;

public record KeywordDto(
    String keyword
) {

    @QueryProjection
    public KeywordDto {
    }

}
