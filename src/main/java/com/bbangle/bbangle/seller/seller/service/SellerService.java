package com.bbangle.bbangle.seller.seller.service;

import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerAccountUpdateRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerStoreNameUpdateRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerUpdateRequest;
import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
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

    @Transactional
    public void createSeller(SellerCreateCommand command, String profileImagePath, Long storeId) {
        // 스토어 객체 생성
        Store store = sellerStoreService.registerStoreForSeller(storeId, command.storeName());
        // 판매자 생성
        sellerRepository.save(
            Seller.create(command.phoneNumber(), command.subPhoneNumber(), command.email(),
                command.originAddress(), command.originAddressDetail(), profileImagePath,
                CertificationStatus.PENDING, store));

    }

}
