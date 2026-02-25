package com.bbangle.bbangle.store.seller.service;

import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.repository.StoreApplicationRepository;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO : 테스트
@Service
@RequiredArgsConstructor
public class SellerStoreApplicationService {

    private final StoreApplicationRepository storeApplicationRepository;

    @Transactional
    public StoreApplication save(
        StoreApplicationRequest.StoreApplicationCreateRequest request,
        String profileImagePath,
        Seller seller,
        Store store
    ) {
        String profile = profileImagePath == null ? request.profile() : profileImagePath;
        return storeApplicationRepository.save(
          StoreApplication.createStoreApplication(
              request.storeName(),
              profile,
              request.introduce(),
              request.phoneNumber(),
              request.subPhoneNumber(),
              request.email(),
              request.originAddress(),
              request.originAddressDetail(),
              seller,
              store
          )
        );
    }

    @Transactional(readOnly = true)
    public Optional<StoreApplication> findStoreApplicationBySellerId(Long sellerId) {
        return storeApplicationRepository.findLatestBySellerId(sellerId);
    }
}
