package com.bbangle.bbangle.push.dto;

import com.querydsl.core.annotations.QueryProjection;

public record FcmPush(
    String fcmToken,
    String memberName,
    String boardTitle,
    Long productId,
    String productTitle,
    String pushCategory
) {

    @QueryProjection
    public FcmPush {
    }
}
