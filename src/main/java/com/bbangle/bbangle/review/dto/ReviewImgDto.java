package com.bbangle.bbangle.review.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;


@Getter
public class ReviewImgDto{
    @JsonInclude(NON_NULL)
    private final Long id;
    @JsonInclude(NON_NULL)
    private final String url;

    @QueryProjection
    @Builder
    public ReviewImgDto(Long id, String url) {
        this.id = id;
        this.url = url;
    }
}
