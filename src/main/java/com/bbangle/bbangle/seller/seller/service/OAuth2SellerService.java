package com.bbangle.bbangle.seller.seller.service;

import com.bbangle.bbangle.auth.oauth.dto.OAuth2Response;
import com.bbangle.bbangle.seller.domain.OAuth2Seller;
import com.bbangle.bbangle.seller.repository.OAuth2SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO : Test
@Service
@RequiredArgsConstructor
public class OAuth2SellerService {

    private final OAuth2SellerRepository oAuth2SellerRepository;

    @Transactional
    public OAuth2Seller createOAuth2Seller(OAuth2Response response) {
        String name = response.getName() == null ? response.getNickname() : response.getName();

        return oAuth2SellerRepository.save(
                OAuth2Seller.create(
                        name,
                        response.getEmail(),
                        response.getProfile(),
                        response.getProvider(),
                        response.getProviderId()
                )
        );
    }
}
