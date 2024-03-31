package com.bbangle.bbangle.token.oauth.infra.kakao.dto;

public record LoginTokenResponse(
    String accessToken,
    String refreshToken
) {

}
