package com.bbangle.bbangle.store.seller.service;

import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.repository.StoreNameRequestRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerStoreService {

    private final StoreRepository storeRepository;
    private final StoreNameRequestRepository storeNameRequestRepository;

    @Transactional(readOnly = true)
    public Store findStore(Long storeId) {
        return storeRepository.findById(storeId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.STORE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Optional<Store> findStoreByStoreName(String storeName) {
        String normalizedStoreName = storeName.strip();
        return storeRepository.findByStoreNameAndIsNotDeleted(normalizedStoreName);
    }

    // TODO : v3 - Admin으로 이동
    @Transactional
    public void registerStore(Seller seller, Store store) {
        seller.registerStore(store);
    }

    @Transactional(readOnly = true)
    public CursorPagination<StoreInfo> selectStoreNameForSeller(String storeName, Long cursorId) {
        String normalizedStoreName = storeName.replaceAll("\\s+", "");

        return storeRepository.findByStoreNameWithCursor(normalizedStoreName, cursorId);
    }

    @Transactional(readOnly = true)
    public Optional<StoreApprovalStatus> findActiveRequestsBySellerId(Seller seller) {
        return storeNameRequestRepository
            .findActiveRequestsBySellerId(
                seller.getId(),
                List.of(StoreApprovalStatus.APPROVE, StoreApprovalStatus.PENDING),
                PageRequest.of(0, 1)
            )
            .stream()
            .findFirst()
            .map(StoreNameRequest::getStatus);
    }

    @Transactional
    public StoreNameRequest updateStoreName(StoreRequest.UpdateStoreNameRequest request, Seller seller) {
        return storeNameRequestRepository.save(
            StoreNameRequest.createStoreNameRequest(seller.getStore(), seller, request.newName())
        );
    }
}
