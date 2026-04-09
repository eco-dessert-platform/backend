package com.bbangle.bbangle.fixture.store.seller.controller.dto;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_DETAIL_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_EMAIL;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_INTRODUCE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_PHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_SUBPHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture.NEW_STORE_NAME;

import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.domain.model.StoreNameRejectCategory;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SellerStoreDetail;
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
            .rejectCategory(null)
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
            .rejectCategory(null)
            .rejectDetail(null)
            .build();
    }

    public static StoreResponse.UpdateStoreNameResponse defaultUpdateStoreNameResponse(
        StoreNameRejectCategory rejectReason,
        String rejectDetail
    ) {
        return UpdateStoreNameResponse.builder()
            .sellerId(1L)
            .storeId(1L)
            .storeNameRequestId(1L)
            .currentName(DEFAULT_STORE_NAME)
            .newName(NEW_STORE_NAME)
            .status(StoreApprovalStatus.REJECT)
            .rejectCategory(rejectReason)
            .rejectDetail(rejectDetail)
            .build();
    }

    public static StoreResponse.SellerStoreDetail defaultSellerStoreDetailResponse(String profile) {
        return new SellerStoreDetail(
            1L,
            NEW_STORE_NAME,
            NEW_INTRODUCE,
            profile,
            NEW_PHONE,
            NEW_SUBPHONE,
            NEW_EMAIL,
            NEW_ADDRESS,
            NEW_DETAIL_ADDRESS
        );
    }
}
