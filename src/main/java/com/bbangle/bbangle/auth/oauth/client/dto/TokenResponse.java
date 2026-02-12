package com.bbangle.bbangle.auth.oauth.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record TokenResponse(
    @Schema(description = "Refresh 토큰") String refreshToken,
    @Schema(description = "Access 토큰") String accessToken
) {
}
