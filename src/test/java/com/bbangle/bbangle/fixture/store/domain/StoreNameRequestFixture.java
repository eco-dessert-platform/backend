package com.bbangle.bbangle.fixture.store.domain;

import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.domain.model.StoreNameRejectCategory;

public class StoreNameRequestFixture {

    public static final String NEW_STORE_NAME = "빵그리의 오븐 1호점";

    private StoreNameRequestFixture() {}

    private static StoreNameRequest baseBuilder(
        String currentName,
        String newName,
        StoreApprovalStatus status,
        StoreNameRejectCategory rejectCategory,
        String rejectDetail,
        Seller seller,
        Store store
    ) {
        return StoreNameRequest.builder()
            .currentName(currentName)
            .newName(newName)
            .status(status)
            .rejectCategory(rejectCategory)
            .rejectDetail(rejectDetail)
            .seller(seller)
            .store(store)
            .build();
    }

    public static StoreNameRequest defaultStoreNameRequest(Seller seller, Store store) {
        return StoreNameRequest.createStoreNameRequest(store, seller, NEW_STORE_NAME);
    }

    public static StoreNameRequest defaultStoreNameRequest(Seller seller, Store store, StoreApprovalStatus status) {
        return baseBuilder(store.getName(), NEW_STORE_NAME, status, null, null, seller, store);
    }
}
