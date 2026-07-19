package com.bbangle.bbangle.fixture.store.admin.service.model;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_DETAIL_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_EMAIL;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_IDENTIFIER;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_INTRODUCE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PROFILE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_SUBPHONE;

import com.bbangle.bbangle.store.admin.service.model.AdminStoreInfo;
import com.bbangle.bbangle.store.domain.StoreApplication;

public class AdminStoreInfoFixture {

    private AdminStoreInfoFixture() {}

    public static AdminStoreInfo withStoreApplication(StoreApplication storeApplication, String identifier) {
        return AdminStoreInfo.builder()
            .storeName(storeApplication.getName())
            .profile(storeApplication.getProfile())
            .introduce(storeApplication.getIntroduce())
            .identifier(identifier)
            .phoneNumber(storeApplication.getPhoneNumberVO().getPhoneNumber())
            .subPhoneNumber(storeApplication.getPhoneNumberVO().getSubPhoneNumber())
            .email(storeApplication.getEmailVO().getEmail())
            .address(storeApplication.getOriginAddressLine())
            .addressDetail(storeApplication.getOriginAddressDetail())
            .build();
    }

    public static AdminStoreInfo defaultAdminStoreInfo() {
        return AdminStoreInfo.builder()
            .storeName(DEFAULT_STORE_NAME)
            .profile(DEFAULT_PROFILE)
            .introduce(DEFAULT_INTRODUCE)
            .identifier(DEFAULT_IDENTIFIER)
            .phoneNumber(DEFAULT_PHONE)
            .subPhoneNumber(DEFAULT_SUBPHONE)
            .email(DEFAULT_EMAIL)
            .address(DEFAULT_ADDRESS)
            .addressDetail(DEFAULT_DETAIL_ADDRESS)
            .build();
    }
}
