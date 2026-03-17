package com.bbangle.bbangle.fixture.store.seller.controller.dto;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture.NEW_STORE_NAME;

import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.domain.model.StoreNameRejectReason;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.UpdateStoreNameResponse;

public class SellerStoreResponseFixture {

    private SellerStoreResponseFixture() {}

    public static StoreResponse.UpdateStoreNameResponse defaultUpdateStoreNameResponse() {
        return UpdateStoreNameResponse.builder()
            .sellerId(1L)
            .storeId(1L)
            .storeNameRequestId(1L)
            .currentName(DEFAULT_STORE_NAME)
            .newName(NEW_STORE_NAME)
            .status(StoreApprovalStatus.PENDING)
            .rejectReason(null)
            .rejectDetail(null)
            .build();
    }

    public static StoreResponse.UpdateStoreNameResponse defaultUpdateStoreNameResponse(StoreApprovalStatus status) {
        return UpdateStoreNameResponse.builder()
            .sellerId(1L)
            .storeId(1L)
            .storeNameRequestId(1L)
            .currentName(DEFAULT_STORE_NAME)
            .newName(NEW_STORE_NAME)
            .status(status)
            .rejectReason(null)
            .rejectDetail(null)
            .build();
    }

    public static StoreResponse.UpdateStoreNameResponse defaultUpdateStoreNameResponse(
        StoreNameRejectReason rejectReason,
        String rejectDetail
    ) {
        return UpdateStoreNameResponse.builder()
            .sellerId(1L)
            .storeId(1L)
            .storeNameRequestId(1L)
            .currentName(DEFAULT_STORE_NAME)
            .newName(NEW_STORE_NAME)
            .status(StoreApprovalStatus.REJECT)
            .rejectReason(rejectReason)
            .rejectDetail(rejectDetail)
            .build();
    }
}
