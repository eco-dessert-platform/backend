package com.bbangle.bbangle.image.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
public class ImageDto {
    @JsonInclude(NON_NULL)
    private final Long id;
    @JsonInclude(NON_NULL)
    private final String url;

    @QueryProjection
    @Builder
    public ImageDto(Long id, String url) {
        this.id = id;
        this.url = url;
    }
}
