package com.bbangle.bbangle.seller.seller.service;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerAccountUpdateRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerStoreNameUpdateRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerUpdateRequest;
import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;

    // TODO : v3 - Store/Seller/SellerStoreService로 이동
    public void updateSeller(SellerUpdateRequest request, Long sellerId) {
        // TODO: 실제 비즈니스 로직 구현
    }

    // TODO : v3 - Store/Seller/SellerStoreService로 이동
    public void updateStoreName(SellerStoreNameUpdateRequest request, Long sellerId) {
        // TODO: 실제 비즈니스 로직 구현
    }

    public void updateAccount(SellerAccountUpdateRequest request, Long sellerId) {
        // TODO: 실제 비즈니스 로직 구현
    }

    @Transactional(readOnly = true)
    public Seller getSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Optional<Seller> findByProviderAndProviderId(OauthServerType provider, String providerId) {
        return sellerRepository.findByProviderAndProviderId(provider, providerId);
    }

    @Transactional
    public Seller createOAuth2Seller(SellerCreateCommand command) {
        String name = command.resolvedName();

        return sellerRepository.save(
            Seller.create(
                name,
                command.provider(),
                command.providerId()
            )
        );
    }

    // TODO : 테스트
    @Transactional(readOnly = true)
    public boolean existsSellerByStoreId(Long storeId) {
        return sellerRepository.existsByStore_Id(storeId);
    }

    // TODO : 테스트
    @Transactional
    public void updateSellerStatus(Seller seller, CertificationStatus status) {
        seller.updateStatus(status);
    }
}
