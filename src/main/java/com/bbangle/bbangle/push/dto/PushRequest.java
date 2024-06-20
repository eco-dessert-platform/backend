package com.bbangle.bbangle.push.dto;

import jakarta.validation.constraints.NotNull;

public record PushRequest(
    @NotNull
    Long boardId,
    @NotNull
    String pushCategory
) {
}
