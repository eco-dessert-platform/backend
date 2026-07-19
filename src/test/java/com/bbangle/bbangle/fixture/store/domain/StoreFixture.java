package com.bbangle.bbangle.fixture.store.domain;

import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.model.EmailVO;
import com.bbangle.bbangle.store.domain.model.PhoneNumberVO;
import org.springframework.test.util.ReflectionTestUtils;

public final class StoreFixture {

    public static final String DEFAULT_STORE_NAME = "test";
    public static final String DEFAULT_IDENTIFIER = "12345";
    public static final String DEFAULT_PROFILE = "test.png";
    public static final String DEFAULT_INTRODUCE = "비건 베이커리";
    public static final String DEFAULT_PHONE = "01012345678";
    public static final String DEFAULT_SUBPHONE = "01098765432";
    public static final String DEFAULT_EMAIL = "123@test.com";
    public static final String DEFAULT_ADDRESS = "서울";
    public static final String DEFAULT_DETAIL_ADDRESS = "123동";

    public static final String NEW_IDENTIFIER = "98765";
    public static final String NEW_PROFILE = "new.jpg";
    public static final String NEW_INTRODUCE = "건강한 디저트 빵그리의 오븐";
    public static final String NEW_PHONE = "01011112222";
    public static final String NEW_SUBPHONE = "01099998888";
    public static final String NEW_EMAIL = "bbanggree@temp.com";
    public static final String NEW_ADDRESS = "한국";
    public static final String NEW_DETAIL_ADDRESS = "가동";

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
        return baseBuilder(DEFAULT_STORE_NAME);
    }

    public static Store defaultStore(String name) {
        return baseBuilder(name);
    }

    public static Store withId(Store store, Long id) {
        ReflectionTestUtils.setField(store, "id", id);
        return store;
    }
}
