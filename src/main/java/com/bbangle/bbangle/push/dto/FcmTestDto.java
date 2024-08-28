package com.bbangle.bbangle.push.dto;

public record FcmTestDto(
    String fcmToken,
    String title,
    String body
) {
}