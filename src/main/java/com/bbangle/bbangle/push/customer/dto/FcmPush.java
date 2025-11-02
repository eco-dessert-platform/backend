package com.bbangle.bbangle.push.customer.dto;

import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.domain.PushType;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

public record FcmPush(
    Long pushId,
    String fcmToken,
    String memberName,
    String boardTitle,
    Long productId,
    String productTitle,
    PushType pushType,
    String days,
    PushCategory pushCategory,
    boolean isSoldOut,
    LocalDateTime date
) {

    @QueryProjection
    public FcmPush {
    }
}
