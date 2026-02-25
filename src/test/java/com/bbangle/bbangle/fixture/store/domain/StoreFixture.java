package com.bbangle.bbangle.fixture.store.domain;

import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.model.EmailVO;
import com.bbangle.bbangle.store.domain.model.PhoneNumberVO;

public final class StoreFixture {

    public static final String DEFAULT_NAME = "test";
    public static final String DEFAULT_IDENTIFIER = "12345";
    public static final String DEFAULT_PROFILE = "test.png";
    public static final String DEFAULT_INTRODUCE = "비건 베이커리";
    public static final String DEFAULT_PHONE = "01012345678";
    public static final String DEFAULT_SUBPHONE = "01098765432";
    public static final String DEFAULT_EMAIL = "123@test.com";
    public static final String DEFAULT_ADDRESS = "서울";
    public static final String DEFAULT_DETAIL_ADDRESS = "123동";

    private StoreFixture() {
    }

    private static Store baseBuilder(String name) {
        return Store.builder()
            .name(name)
            .identifier(DEFAULT_IDENTIFIER)
            .profile(DEFAULT_PROFILE)
            .introduce(DEFAULT_INTRODUCE)
            .phoneNumberVO(PhoneNumberVO.of(DEFAULT_PHONE, DEFAULT_SUBPHONE))
            .emailVO(EmailVO.of(DEFAULT_EMAIL))
            .originAddressLine(DEFAULT_ADDRESS)
            .originAddressDetail(DEFAULT_DETAIL_ADDRESS)
            .build();
    }

    public static Store defaultStore() {
        return baseBuilder(DEFAULT_NAME);
    }

    public static Store defaultStore(String name) {
        return baseBuilder(name);
    }

    // TODO : 리팩토링 할 수 있는 거 추가하기
}
