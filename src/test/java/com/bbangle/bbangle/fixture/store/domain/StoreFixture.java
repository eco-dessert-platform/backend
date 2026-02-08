package com.bbangle.bbangle.fixture.store.domain;

import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.domain.model.EmailVO;
import com.bbangle.bbangle.store.domain.model.PhoneNumberVO;

public final class StoreFixture {

    private StoreFixture() {
    }

    public static Store defaultStore() {
        return Store.builder()
            .name("빵그리의 오븐")
            .profile("test.png")
            .introduce("비건 베이커리")
            .phoneNumberVO(
                PhoneNumberVO.of("01012345678", "01098765432")
            )
            .emailVO(EmailVO.of("123@test.com"))
            .originAddressLine("서울")
            .originAddressDetail("123동")
            .status(StoreStatus.NONE)
            .build();
    }

    public static Store defaultStore(StoreStatus status) {
        return Store.builder()
            .name("빵그리의 오븐")
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

    // TODO : 리팩토링 할 수 있는 거 추가하기
}
