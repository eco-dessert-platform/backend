package com.bbangle.bbangle.push.dto;

public record CreatePushRequest(
    String fcmToken,
    String pushType,
    String days,
    String pushCategory,
    Long productId
) {
}
