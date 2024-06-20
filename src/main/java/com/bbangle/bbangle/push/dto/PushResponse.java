package com.bbangle.bbangle.push.dto;

import com.querydsl.core.annotations.QueryProjection;

public record PushResponse(
    String storeName,
    String boardTitle,
    String boardThumbnail,
    boolean pushStatus
) {

    @QueryProjection
    public PushResponse{}

}
