package com.bbangle.bbangle.store.seller.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
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

}
