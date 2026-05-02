package com.bbangle.bbangle.fixture.store.seller.controller.dto;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_DETAIL_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_EMAIL;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_INTRODUCE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_PHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_SUBPHONE;
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

    private static StoreRequest.UpdateStoreDetailRequest baseUpdateStoreDetailRequest(
        String introduce,
        String phone,
        String subPhone,
        String email,
        String address,
        String detailAddress
    ) {
        return new StoreRequest.UpdateStoreDetailRequest(
            introduce,
            phone,
            subPhone,
            email,
            address,
            detailAddress
        );
    }

    public static StoreRequest.UpdateStoreDetailRequest defaultUpdateStoreDetailRequest(
        String introduce,
        String subPhone
    ) {
        return baseUpdateStoreDetailRequest(
            introduce,
            NEW_PHONE,
            subPhone,
            NEW_EMAIL,
            NEW_ADDRESS,
            NEW_DETAIL_ADDRESS
        );
    }

    public static StoreRequest.UpdateStoreDetailRequest defaultUpdateStoreDetailRequest(
    ) {
        return baseUpdateStoreDetailRequest(
            NEW_INTRODUCE,
            NEW_PHONE,
            NEW_SUBPHONE,
            NEW_EMAIL,
            NEW_ADDRESS,
            NEW_DETAIL_ADDRESS
        );
    }
}
