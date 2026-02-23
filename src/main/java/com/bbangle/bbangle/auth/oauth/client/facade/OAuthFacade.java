package com.bbangle.bbangle.auth.oauth.client.facade;

import com.bbangle.bbangle.auth.oauth.client.dto.TokenClaimsDTO;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenResponse;
import com.bbangle.bbangle.auth.oauth.client.service.OAuthService;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthFacade {

    private final OAuthService oAuthService;
    private final TokenProvider tokenProvider;

    public TokenResponse reissueToken(String refreshToken) {

        TokenClaimsDTO claims = tokenProvider.parseRefreshToken(refreshToken);

        oAuthService.refreshTokenValidate(refreshToken);
        oAuthService.deleteRefreshToken(refreshToken);

        String newRefreshToken = oAuthService.generateRefreshToken(claims.id(), claims.role());
        String newAccessToken = oAuthService.generateAccessToken(claims.id(), claims.role());

        return TokenResponse.builder()
            .refreshToken(newRefreshToken)
            .accessToken(newAccessToken)
            .build();
    }
}
