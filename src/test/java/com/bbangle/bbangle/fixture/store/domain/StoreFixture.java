package com.bbangle.bbangle.fixture.store.domain;

import com.bbangle.bbangle.store.domain.Store;

public final class StoreFixture {

    private StoreFixture() {
    }

    public static Store defaultStore() {
        return Store.createForSeller(
            "빵그리의 오븐",
            "test.png",
            "비건 베이커리",
            "01012345678",
            "01098765432",
            "123@test.com",
            "서울",
            "123동"
        );
    }

    // TODO : 리팩토링 할 수 있는 거 추가하기
}
