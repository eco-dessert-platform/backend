package com.bbangle.bbangle.fixture.store.seller.service.model;

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
}
