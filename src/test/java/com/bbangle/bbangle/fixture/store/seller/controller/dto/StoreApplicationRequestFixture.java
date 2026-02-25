package com.bbangle.bbangle.fixture.store.seller.controller.dto;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_DETAIL_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_EMAIL;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_INTRODUCE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_SUBPHONE;

import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationRequest.StoreApplicationCreateRequest;

public class StoreApplicationRequestFixture {

    private StoreApplicationRequestFixture() {}

    public static StoreApplicationRequest.StoreApplicationCreateRequest defaultStoreApplicationCreateRequest(
        String profile, Long storeId
    ) {
        return new StoreApplicationCreateRequest(
            DEFAULT_STORE_NAME,
            profile,
            DEFAULT_INTRODUCE,
            DEFAULT_PHONE,
            DEFAULT_SUBPHONE,
            DEFAULT_EMAIL,
            DEFAULT_ADDRESS,
            DEFAULT_DETAIL_ADDRESS,
            storeId
        );
    }
}
