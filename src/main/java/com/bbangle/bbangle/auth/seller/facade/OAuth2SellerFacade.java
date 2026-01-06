package com.bbangle.bbangle.auth.seller.facade;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2Response;
import com.bbangle.bbangle.seller.domain.OAuth2Seller;
import com.bbangle.bbangle.seller.seller.service.OAuth2SellerService;
import com.bbangle.bbangle.seller.seller.service.command.OAuth2ResponseCreateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

// TODO : test
@Service
@RequiredArgsConstructor
public class OAuth2SellerFacade {

    private final OAuth2SellerService oAuth2SellerService;

    public OAuth2Seller login(OAuth2Response response) {
        try {
            return oAuth2SellerService.findByProviderAndProviderId(
                    response.getProvider(),
                    response.getProviderId()
            ).orElseGet(() ->
                    oAuth2SellerService.createOAuth2Seller(OAuth2ResponseCreateCommand.from(response))
            );
        } catch (DataIntegrityViolationException e) {   // UNIQUE(providerId) 충돌 시 한번 더 조회
            return oAuth2SellerService.findByProviderAndProviderId(
                    response.getProvider(),
                    response.getProviderId()
            ).orElseThrow(() -> e);
        }
    }
}
