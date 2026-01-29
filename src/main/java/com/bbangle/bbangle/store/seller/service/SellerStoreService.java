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
import java.util.List;
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

    // TODO : Test
    @Transactional(readOnly = true)
    public CursorPagination<StoreInfo> selectStoreNameForSeller(String storeName){
        if (storeName.isBlank()) {
            throw new BbangleException(BbangleErrorCode.INVALID_STORE_NAME);
        }
        String normalizedStoreName = storeName.replaceAll("\\s+", "");

        /// 페이징 처리를 위한 +1 조회 진행
        ///중복되지 않은 스토어명들을 조회하고 id 값을 List로 모아 페이징 처리 로직으로 전달
        List<Long> storeIds = storeRepository.getStoreByStoreName(normalizedStoreName).stream().map(Store::getId).toList();

        // 2. 스토어 명이 중복이 아니라면 사용 가능하다
         return storeRepository.findNextCursorPage(storeIds);
    }

    // TODO : Test
    public StoreResponse.StoreNameCheck checkStoreName(String storeName) {
        return storeRepository.findByStoreNameAndIsNotDeleted(storeName)
            .map(store ->
                StoreResponse.StoreNameCheck.builder()
                    .duplicated(!StoreStatus.NONE.equals(store.getStatus()))
                    .store(sellerStoreMapper.toSellerStoreDetail(store))
                    .build()
            )
            .orElseGet(() ->
                StoreResponse.StoreNameCheck.builder()
                    .duplicated(false)
                    .store(null)
                    .build()
            );
    }
}
