package com.bbangle.bbangle.push.dto;

import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.domain.PushType;
import com.querydsl.core.annotations.QueryProjection;

public record FcmPush(
    Long pushId,
    String fcmToken,
    String memberName,
    String boardTitle,
    Long productId,
    String productTitle,
    PushType pushType,
    String days,
    PushCategory pushCategory
) {

    @QueryProjection
    public FcmPush {
    }
}
