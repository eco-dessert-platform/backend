package com.bbangle.bbangle.token.oauth.infra.kakao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginTokenResponse(
    @Schema(description = "Access 토큰") String accessToken,
    @Schema(description = "Refresh 토큰") String refreshToken
) {

}
