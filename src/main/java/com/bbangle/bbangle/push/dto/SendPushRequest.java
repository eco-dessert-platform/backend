package com.bbangle.bbangle.push.dto;

public record SendPushRequest(
    String token,
    String title,
    String body
) {
}
