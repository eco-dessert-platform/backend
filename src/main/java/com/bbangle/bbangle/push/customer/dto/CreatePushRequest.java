package com.bbangle.bbangle.push.customer.dto;

import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.domain.PushType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CreatePushRequest(
    @NotNull
    @Schema(description = "FCM 디바이스 토큰(디바이스 식별)", example = "abc123xyz")
    String fcmToken,
    @Schema(description = "푸시 알림 타입")
    PushType pushType,
    @Schema(description = "푸시 반복 요일 (예: MONDAY,TUESDAY,WEDNESDAY, ...)")
    String days,
    @NotNull
    @Schema(description = "푸시 카테고리 타입")
    PushCategory pushCategory,
    @NotNull
    @Schema(description = "상품 ID", example = "12")
    Long productId
) {

}
