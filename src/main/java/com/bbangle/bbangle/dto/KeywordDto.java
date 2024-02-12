package com.bbangle.bbangle.dto;

import com.bbangle.bbangle.model.Search;
import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record KeywordDto(
    String keyword
) {
    @QueryProjection
    public KeywordDto(String keyword) {
        this.keyword=keyword;
    }
}