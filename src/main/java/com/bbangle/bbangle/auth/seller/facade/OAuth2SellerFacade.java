package com.bbangle.bbangle.auth.seller.facade;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.auth.oauth.client.service.OAuthService;
import com.bbangle.bbangle.auth.seller.facade.dto.GenerateTokenDTO;
import com.bbangle.bbangle.auth.seller.service.OAuthSellerService;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2SellerFacade {

    // OAuth 작업과 관련된 Service 레이어
    private final OAuthSellerService oAuthSellerService;
    private final SellerService sellerService;
    private final OAuthService oAuthService;

    public Seller login(SellerCreateCommand response) {
        try {
            return sellerService.findByProviderAndProviderId(
                    response.provider(),
                    response.providerId()
            ).orElseGet(() ->
                sellerService.createOAuth2Seller(response)
            );
        } catch (DataIntegrityViolationException e) {   // UNIQUE(providerId) 충돌 시 한번 더 조회
            return sellerService.findByProviderAndProviderId(
                    response.provider(),
                    response.providerId()
            ).orElseThrow(() -> e);
        }
    }

    public GenerateTokenDTO generateToken(String code) {

        OAuth2DTO.InfoDTO sellerInfo = oAuthSellerService.getSellerInfoFromRedis(code);
        String refreshToken = oAuthService.generateRefreshToken(sellerInfo.id(), sellerInfo.role());
        String accessToken = oAuthService.generateAccessToken(sellerInfo.id(), sellerInfo.role());

        return GenerateTokenDTO.of(
            refreshToken,
            accessToken,
            sellerInfo.id(),
            sellerInfo.status()
        );
    }
}
