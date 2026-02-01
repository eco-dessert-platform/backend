package com.bbangle.bbangle.auth.seller.facade;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2InfoRedisDTO;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenClaimsDTO;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenResponse;
import com.bbangle.bbangle.auth.seller.facade.dto.GenerateTokenDTO;
import com.bbangle.bbangle.auth.seller.service.OAuthSellerService;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.seller.domain.OAuth2Seller;
import com.bbangle.bbangle.seller.seller.service.OAuth2SellerService;
import com.bbangle.bbangle.seller.seller.service.command.OAuth2ResponseCreateCommand;
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

    public GenerateTokenDTO generateToken(String code) {

        OAuth2InfoRedisDTO sellerInfo = oAuthSellerService.getSellerInfoFromRedis(code);
        String refreshToken = oAuthSellerService.generateRefreshToken(sellerInfo.id(), sellerInfo.role());
        String accessToken = oAuthSellerService.generateAccessToken(sellerInfo.id(), sellerInfo.role());

        return GenerateTokenDTO.of(
            refreshToken,
            accessToken,
            sellerInfo.id(),
            sellerInfo.status()
        );
    }

    public TokenResponse reissueToken(String refreshToken) {

        TokenClaimsDTO claims = tokenProvider.parseRefreshToken(refreshToken);

        oAuthSellerService.refreshTokenValidate(refreshToken);
        oAuthSellerService.deleteRefreshToken(refreshToken);

        String newRefreshToken = oAuthSellerService.generateRefreshToken(claims.id(), claims.role());
        String newAccessToken = oAuthSellerService.generateAccessToken(claims.id(), claims.role());

        return TokenResponse.builder()
            .refreshToken(newRefreshToken)
            .accessToken(newAccessToken)
            .build();
    }
}
