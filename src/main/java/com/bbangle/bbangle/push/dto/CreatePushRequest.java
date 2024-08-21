package com.bbangle.bbangle.push.dto;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.domain.PushType;
import jakarta.validation.constraints.NotNull;

public record CreatePushRequest(
    @NotNull
    String fcmToken,
    @NotNull
    PushType pushType,
    String days,
    @NotNull
    PushCategory pushCategory,
    String date,
    @NotNull
    Long productId
) {
}
