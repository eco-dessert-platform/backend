package com.bbangle.bbangle.fixture.store.admin.controller.dto;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_DETAIL_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_EMAIL;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_IDENTIFIER;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_INTRODUCE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_PHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_SUBPHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture.NEW_STORE_NAME;

import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest.StoreDetailRequest;

public class StoreDetailRequestFixture {

    private StoreDetailRequestFixture() {}

    public static StoreDetailRequest defaultStoreDetailRequestFixture() {
        return new StoreDetailRequest(
            NEW_STORE_NAME,
            NEW_IDENTIFIER,
            NEW_INTRODUCE,
            NEW_PHONE,
            NEW_SUBPHONE,
            NEW_EMAIL,
            NEW_ADDRESS,
            NEW_DETAIL_ADDRESS
        );
    }

    public static StoreDetailRequest defaultStoreDetailRequestFixture(String storeName) {
        return new StoreDetailRequest(
            storeName,
            NEW_IDENTIFIER,
            NEW_INTRODUCE,
            NEW_PHONE,
            NEW_SUBPHONE,
            NEW_EMAIL,
            NEW_ADDRESS,
            NEW_DETAIL_ADDRESS
        );
    }
}
