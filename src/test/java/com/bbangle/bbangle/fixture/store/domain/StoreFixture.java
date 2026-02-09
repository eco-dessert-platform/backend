package com.bbangle.bbangle.fixture.store.domain;

import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.domain.model.EmailVO;
import com.bbangle.bbangle.store.domain.model.PhoneNumberVO;

public final class StoreFixture {

    private StoreFixture() {
    }

    private static Store basebuilder(String name, StoreStatus status) {
        return Store.builder()
            .name(name)
            .profile("test.png")
            .introduce("비건 베이커리")
            .phoneNumberVO(
                PhoneNumberVO.of("01012345678", "01098765432")
            )
            .emailVO(EmailVO.of("123@test.com"))
            .originAddressLine("서울")
            .originAddressDetail("123동")
            .status(status)
            .build();
    }

    public static Store defaultStore() {
        return basebuilder("빵그리의 오븐", StoreStatus.NONE);
    }

    public static Store defaultStore(StoreStatus status) {
        return basebuilder("빵그리의 오븐", status);
    }

    // TODO : 리팩토링 할 수 있는 거 추가하기
}
