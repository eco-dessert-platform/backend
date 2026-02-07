package com.bbangle.bbangle.store.seller.service;

import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.mapper.SellerStoreMapper;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerStoreService {

    private final StoreRepository storeRepository;
    private final SellerStoreMapper sellerStoreMapper;

    @Transactional
    public Store createStore(StoreRequest.StoreCreateRequest request, String profileImagePath) {
        // 1. DB에 존재하는 스토어를 사용하는 경우
        if (request.storeId() != null) {
            Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.STORE_NOT_FOUND));

            store.updateDetail(
                request.profile(),
                request.shortDescription(),
                request.phoneNumber(),
                request.subPhoneNumber(),
                request.email(),
                request.originAddress(),
                request.originAddressDetail()
            );

            return store;
        }

        // 2. 새로운 스토어 생성하는 경우
        if (profileImagePath == null || profileImagePath.isEmpty()) throw new BbangleException(BbangleErrorCode.INVALID_PROFILE);

        return storeRepository.save(
            Store.createForSeller(
                request.storeName(),
                profileImagePath,
                request.shortDescription(),
                request.phoneNumber(),
                request.subPhoneNumber(),
                request.email(),
                request.originAddress(),
                request.originAddressDetail()
            )
        );
    }

    @Transactional
    public void registerStore(Seller seller, Store store) {
        seller.registerStore(store);
    }

    @Transactional(readOnly = true)
    public CursorPagination<StoreInfo> selectStoreNameForSeller(String storeName, Long cursorId) {
        String normalizedStoreName = storeName.replaceAll("\\s+", "");

        return storeRepository.findByStoreNameWithCursor(normalizedStoreName, cursorId);
    }

    public StoreResponse.StoreNameCheck checkStoreName(String storeName) {
        String normalizedStoreName = storeName.strip();

        return storeRepository.findByStoreNameAndIsNotDeleted(normalizedStoreName)
            .map(store ->
                StoreResponse.StoreNameCheck.builder()
                    .available(StoreStatus.NONE.equals(store.getStatus()))
                    .store(sellerStoreMapper.toSellerStoreDetail(store))
                    .build()
            )
            .orElseGet(() ->
                StoreResponse.StoreNameCheck.builder()
                    .available(true)
                    .store(null)
                    .build()
            );
    }
}
