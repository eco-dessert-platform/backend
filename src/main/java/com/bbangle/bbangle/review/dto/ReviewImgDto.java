package com.bbangle.bbangle.review.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;


@Getter
public class ReviewImgDto{
    private final Long id;
    private final String url;

    @QueryProjection
    @Builder
    public ReviewImgDto(Long id, String url) {
        this.id = id;
        this.url = url;
    }
}
