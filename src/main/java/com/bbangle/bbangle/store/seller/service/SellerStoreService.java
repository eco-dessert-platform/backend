package com.bbangle.bbangle.store.seller.service;

import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.repository.StoreRepository;
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
    public Store registerStoreForSeller(Long storeId, String storeName) {
        // 1. 크롤링된 스토어 사용하는 경우
        if (storeId != null) {
            return storeRepository.findById(storeId)
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.STORE_NOT_FOUND));
        }
        // 중복검사 진행
        if (storeRepository.existsByStoreName(storeName)) {
            throw new BbangleException(BbangleErrorCode.INVALID_STORE_NAME);
        }

        // 2. 새로운 스토어 생성하는 경우
        return storeRepository.save(Store.createForSeller(storeName));
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
