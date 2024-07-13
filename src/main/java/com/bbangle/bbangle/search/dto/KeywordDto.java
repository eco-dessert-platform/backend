package com.bbangle.bbangle.search.dto;

import com.querydsl.core.annotations.QueryProjection;

public class KeywordDto {

    String keyword;

    @QueryProjection
    public KeywordDto(String keyword) {
        this.keyword = keyword;
    }

}
