package com.bbangle.bbangle.store.seller.facade;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest.UpdateStoreNameRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SellerStoreAvailable;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.UpdateStoreNameResponse;
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

    public SellerStoreAvailable checkStoreName(String storeName) {
        Optional<Store> optionalStore = sellerStoreService.findStoreByStoreName(storeName);
        if (optionalStore.isEmpty()) {
            return SellerStoreAvailable.builder()
                .available(true)
                .store(null)
                .build();
        }

        Store store = optionalStore.get();
        return SellerStoreAvailable.builder()
            .available(!sellerService.existsSellerByStoreId(store.getId()))
            .store(sellerStoreMapper.toSellerStoreDetail(store))
            .build();
    }

    public SellerStoreAvailable getRegisteredStoreDetail(Long sellerId) {
        Seller seller = sellerService.getSellerById(sellerId);
        if (seller.getStore() == null) throw new BbangleException(BbangleErrorCode.NOT_REGISTERED_STORE);

        Optional<StoreApprovalStatus> status = sellerStoreService.findActiveRequestsBySellerId(seller);

        return SellerStoreAvailable.builder()
            .available(status.isEmpty())
            .store(sellerStoreMapper.toSellerStoreDetail(seller.getStore()))
            .build();
    }

    public UpdateStoreNameResponse updateStoreName(Long sellerId, UpdateStoreNameRequest request) {
        Seller seller = sellerService.getSellerById(sellerId);

        Optional<StoreApprovalStatus> status = sellerStoreService.findActiveRequestsBySellerId(seller);
        status.ifPresent(s -> {
            switch (s) {
                case APPROVE -> throw new BbangleException(BbangleErrorCode.ALREADY_UPDATE_STORE_NAME);
                case PENDING -> throw new BbangleException(BbangleErrorCode.REQUEST_IS_PENDING);
            }
        });

        if (sellerStoreService.findStoreByStoreName(request.newName()).isPresent()) {
            throw new BbangleException(BbangleErrorCode.ALREADY_RESERVED_STORE);
        }

        return sellerStoreMapper.toUpdateStoreNameResponse(
            sellerStoreService.updateStoreName(request, seller)
        );
    }
}
