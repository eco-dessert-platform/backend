package com.bbangle.bbangle.store.seller.service;

import com.bbangle.bbangle.common.page.StoreCustomPage;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerStoreService {

    private final StoreRepository storeRepository;

    @Transactional
    public Store registerStoreForSeller(Long storeId, String storeName) {
        // 기존 스토어를 사용하는 경우
        if (storeId != null) {
            return storeRepository.findById(storeId)
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.STORE_NOT_FOUND));
        }
        // 중복검사 진행
        if (storeRepository.findByStoreName(storeName).isPresent()) {
            throw new BbangleException(BbangleErrorCode.INVALID_STORE_NAME);
        }

        // 신규 스토어 생성(이름이 중복되지 않는 경우)
        return storeRepository.save(Store.createForSeller(storeName));
    }

    @Transactional
    public StoreCustomPage<List<StoreInfo>> selectStoreNameForSeller(String storeName, Long cursorId){
        String normalizedStoreName = normalize(storeName);
        // 1. 스토어명이 중복이라면 사용할 수없다.
        if (storeRepository.findByStoreName(normalizedStoreName).isPresent()) {
            throw new BbangleException(BbangleErrorCode.INVALID_STORE_NAME);
        }
        // 2. 스토어 명이 중복이 아니라면 사용 가능하다
         return storeRepository.findNextCursorPage(cursorId, normalizedStoreName);
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        // → "   " 같은 공백-only 문자열이면 null로 간주
        return trimmed.isEmpty() ? null : trimmed;
    }

}
