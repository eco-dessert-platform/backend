package com.bbangle.bbangle.seller.seller.service;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerAccountUpdateRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerStoreNameUpdateRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerUpdateRequest;
import com.bbangle.bbangle.seller.seller.service.command.OAuth2ResponseCreateCommand;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final SellerStoreService sellerStoreService;

    public void updateSeller(SellerUpdateRequest request, Long sellerId) {
        // TODO: 실제 비즈니스 로직 구현
    }

    public void updateStoreName(SellerStoreNameUpdateRequest request, Long sellerId) {

        // TODO: 실제 비즈니스 로직 구현

    }

    public void updateAccount(SellerAccountUpdateRequest request, Long sellerId) {

        // TODO: 실제 비즈니스 로직 구현

    }

    // TODO : 테스트하기
    public Seller getSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND));
    }

    // TODO : 테스트하기
    public Optional<Seller> findByProviderAndProviderId(OauthServerType provider, String providerId) {
        return sellerRepository.findByProviderAndProviderId(provider, providerId);
    }

    // TODO : OAuth2SellerService에서 이동하였으므로 테스트 코드 수정하기
    @Transactional
    public Seller createOAuth2Seller(OAuth2ResponseCreateCommand command) {
        String name = command.resolvedName();

        return sellerRepository.save(
            Seller.create(
                name,
                command.provider(),
                command.providerId()
            )
        );
    }

}
