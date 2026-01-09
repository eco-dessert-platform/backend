package com.bbangle.bbangle.fixture.store.domain;

import com.bbangle.bbangle.store.domain.Store;

public final class StoreFixture {

    private StoreFixture() {
    }

    public static Store defaultStore() {
        return Store.builder()
            .name("가게명")
            .build();
    }

}
