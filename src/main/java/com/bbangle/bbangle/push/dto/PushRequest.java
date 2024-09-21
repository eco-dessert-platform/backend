package com.bbangle.bbangle.push.dto;

import com.bbangle.bbangle.push.domain.PushCategory;
import jakarta.validation.constraints.NotNull;

public record PushRequest(
    @NotNull
    Long productId,
    @NotNull
    String pushType,
    @NotNull
    PushCategory pushCategory
) {
}
