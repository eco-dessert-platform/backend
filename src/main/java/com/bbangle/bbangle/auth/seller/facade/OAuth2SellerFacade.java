package com.bbangle.bbangle.auth.seller.facade;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2InfoRedisDTO;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenResponse;
import com.bbangle.bbangle.auth.seller.controller.dto.GenerateTokenResponse;
import com.bbangle.bbangle.auth.seller.service.OAuthSellerService;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.seller.domain.OAuth2Seller;
import com.bbangle.bbangle.seller.seller.service.OAuth2SellerService;
import com.bbangle.bbangle.seller.seller.service.command.OAuth2ResponseCreateCommand;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2SellerFacade {

    // 임시 Seller 테이블용 Service 레이어
    private final OAuth2SellerService oAuth2SellerService;

    // OAuth 작업과 관련된 Service 레이어
    private final OAuthSellerService oAuthSellerService;

    private final TokenProvider tokenProvider;

    public OAuth2Seller login(OAuth2ResponseCreateCommand response) {
        try {
            return oAuth2SellerService.findByProviderAndProviderId(
                    response.provider(),
                    response.providerId()
            ).orElseGet(() ->
                    oAuth2SellerService.createOAuth2Seller(response)
            );
        } catch (DataIntegrityViolationException e) {   // UNIQUE(providerId) 충돌 시 한번 더 조회
            return oAuth2SellerService.findByProviderAndProviderId(
                    response.provider(),
                    response.providerId()
            ).orElseThrow(() -> e);
        }
    }

    public GenerateTokenResponse generateToken(String code) {

        OAuth2InfoRedisDTO sellerInfo = oAuthSellerService.getSellerInfoFromRedis(code);
        String refreshToken = oAuthSellerService.generateRefreshToken(sellerInfo.id(), sellerInfo.role());
        String accessToken = oAuthSellerService.generateAccessToken(sellerInfo.id(), sellerInfo.role());

        return GenerateTokenResponse.of(
            refreshToken,
            accessToken,
            sellerInfo.id(),
            sellerInfo.status()
        );
    }

    // TODO : Test
    public TokenResponse reissueToken(String refreshToken) {

        // 1. Refresh Token 분석 -> 만료된 토큰일 경우 예외 던짐
        Claims claims = tokenProvider.parseRefreshToken(refreshToken);

        // 2. Refresh Token이 DB에 없을 경우 예외
        oAuthSellerService.refreshTokenValidate(refreshToken);

        Long id = claims.get("id", Long.class);
        Role role = Role.from(claims.get("role", String.class));

        // 3. 만료되지 않았을 경우 Refresh Token 새로 생성
        String newRefreshToken = oAuthSellerService.generateRefreshToken(id, role);

        // 4. 만료되지 않았을 경우 Access Token 새로 생성
        String newAccessToken = oAuthSellerService.generateAccessToken(id, role);

        // 5. DTO 생성 및 반환
        return TokenResponse.builder()
            .refreshToken(newRefreshToken)
            .accessToken(newAccessToken)
            .build();
    }
}
