package com.bbangle.bbangle.push.dto;

import com.querydsl.core.annotations.QueryProjection;

public record PushResponse(
    Long productId,
    String storeName,
    String productTitle,
    String boardThumbnail,
    boolean subscribed
) {

    @QueryProjection
    public PushResponse{}

}
