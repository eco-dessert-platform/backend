package com.bbangle.bbangle.push.dto;

import com.bbangle.bbangle.push.domain.PushCategory;
import com.querydsl.core.annotations.QueryProjection;

public record FcmPush(
    String fcmToken,
    String memberName,
    String boardTitle,
    Long productId,
    String productTitle,
    PushCategory pushCategory
) {

    @QueryProjection
    public FcmPush {
    }
}
