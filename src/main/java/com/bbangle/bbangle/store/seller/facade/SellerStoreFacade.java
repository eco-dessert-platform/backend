package com.bbangle.bbangle.store.seller.facade;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
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

    public StoreResponse.SellerStoreDTO getRegisteredStoreDetail(Long sellerId) {
        Seller seller = sellerService.getSellerById(sellerId);

        if (seller.getStore() == null) throw new BbangleException(BbangleErrorCode.NOT_REGISTERED_STORE);

        return StoreResponse.SellerStoreDTO.builder()
            .sellerId(seller.getId())
            .store(sellerStoreMapper.toSellerStoreDetail(seller.getStore()))
            .build();
    }
}
