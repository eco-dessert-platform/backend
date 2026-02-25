package com.bbangle.bbangle.fixture.store.domain;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_DETAIL_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_EMAIL;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_INTRODUCE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PROFILE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_SUBPHONE;

import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreApplication;

public class StoreApplicationFixture {

    private StoreApplicationFixture() {}

    private static StoreApplication baseBuilder(String name, Seller seller, Store store) {
        return StoreApplication.createStoreApplication(
            name,
            DEFAULT_PROFILE,
            DEFAULT_INTRODUCE,
            DEFAULT_PHONE,
            DEFAULT_SUBPHONE,
            DEFAULT_EMAIL,
            DEFAULT_ADDRESS,
            DEFAULT_DETAIL_ADDRESS,
            seller,
            store
        );
    }

    public static StoreApplication defaultStoreApplication(Seller seller, Store store) {
        return baseBuilder(DEFAULT_STORE_NAME, seller, store);
    }

    public static StoreApplication defaultStoreApplication(String name, Seller seller, Store store) {
        return baseBuilder(name, seller, store);
    }
}
