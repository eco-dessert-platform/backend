package com.bbangle.bbangle.store.seller.facade;

import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.StoreNameCheck;
import com.bbangle.bbangle.store.seller.controller.mapper.SellerStoreMapper;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerStoreFacade {

    private final SellerStoreService sellerStoreService;
    private final SellerService sellerService;
    private final SellerStoreMapper sellerStoreMapper;

    // TODO : 테스트
    public StoreNameCheck checkStoreName(String storeName) {
        Optional<Store> optionalStore = sellerStoreService.findStoreByStoreName(storeName);
        if (optionalStore.isEmpty()) {
            return StoreNameCheck.builder()
                .available(true)
                .store(null)
                .build();
        }

        Store store = optionalStore.get();
        return StoreNameCheck.builder()
            .available(!sellerService.existsSellerByStoreId(store.getId()))
            .store(sellerStoreMapper.toSellerStoreDetail(store))
            .build();
    }
}
