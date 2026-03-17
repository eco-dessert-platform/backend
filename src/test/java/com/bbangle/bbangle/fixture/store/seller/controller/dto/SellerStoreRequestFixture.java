package com.bbangle.bbangle.fixture.store.seller.controller.dto;

import static com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture.NEW_STORE_NAME;

import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest;

public class SellerStoreRequestFixture {

    private SellerStoreRequestFixture() {}

    public static StoreRequest.UpdateStoreNameRequest defaultUpdateStoreNameRequest() {
        return new StoreRequest.UpdateStoreNameRequest(NEW_STORE_NAME);
    }

    public static StoreRequest.UpdateStoreNameRequest defaultUpdateStoreNameRequest(String newName) {
        return new StoreRequest.UpdateStoreNameRequest(newName);
    }
}
