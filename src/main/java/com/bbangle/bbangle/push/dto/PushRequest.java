package com.bbangle.bbangle.push.dto;

import jakarta.validation.constraints.NotNull;

public record PushRequest(
    @NotNull
    Long productId,
    @NotNull
    String pushType,
    @NotNull
    String pushCategory
) {
}
