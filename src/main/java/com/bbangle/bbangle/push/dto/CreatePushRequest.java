package com.bbangle.bbangle.push.dto;

import jakarta.validation.constraints.NotNull;

public record CreatePushRequest(
    @NotNull
    String fcmToken,
    @NotNull
    String pushCategory,
    @NotNull
    Long storeId,
    @NotNull
    Long boardId,
    @NotNull
    Long productId
) {
}
