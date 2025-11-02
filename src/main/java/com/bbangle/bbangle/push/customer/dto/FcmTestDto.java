package com.bbangle.bbangle.push.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FcmTestDto(
    @Schema(description = "FCM 디바이스 토큰(디바이스 식별)", example = "abc123xyz")
    String fcmToken,
    @Schema(description = "메세지 타이틀", example = "알림 문자")
    String title,
    @Schema(description = "메세지 본문", example = "안녕하세요 고객님...")
    String body
) {

}
