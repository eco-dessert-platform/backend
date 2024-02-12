package com.bbangle.bbangle.dto;

import com.bbangle.bbangle.model.Search;
import com.querydsl.core.annotations.QueryProjection;

public record KeywordDto(
    Long keywordId,
    String keyword
) {
    @QueryProjection
    public KeywordDto(Long keywordId, String keyword) {
        this.keywordId=keywordId;
        this.keyword=keyword;
    }
}