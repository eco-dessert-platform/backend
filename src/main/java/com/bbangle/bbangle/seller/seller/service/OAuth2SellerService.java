package com.bbangle.bbangle.seller.seller.service;

import com.bbangle.bbangle.seller.domain.OAuth2Seller;
import com.bbangle.bbangle.seller.repository.OAuth2SellerRepository;
import com.bbangle.bbangle.seller.seller.service.command.OAuth2ResponseCreateCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO : Test
@Service
@RequiredArgsConstructor
public class OAuth2SellerService {

    private final OAuth2SellerRepository oAuth2SellerRepository;

    @Transactional
    public OAuth2Seller createOAuth2Seller(OAuth2ResponseCreateCommand command) {
        String name = command.name() == null ? command.nickname() : command.name();

        return oAuth2SellerRepository.save(
                OAuth2Seller.create(
                        name,
                        command.provider(),
                        command.providerId()
                )
        );
    }
}
