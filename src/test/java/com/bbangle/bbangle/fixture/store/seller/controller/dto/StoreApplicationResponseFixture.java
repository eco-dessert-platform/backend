package com.bbangle.bbangle.fixture.store.seller.controller.dto;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_DETAIL_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_EMAIL;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_INTRODUCE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PROFILE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_SUBPHONE;

import com.bbangle.bbangle.store.domain.model.StoreApplicationStatus;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationResponse.StoreApplicationDetail;
import java.time.LocalDateTime;

public class StoreApplicationResponseFixture {

    StoreApplicationResponseFixture() {}

    public static StoreApplicationDetail defaultStoreApplicationDetail(Long storeId) {
        return StoreApplicationDetail.builder()
            .storeApplicationId(1L)
            .sellerId(1L)
            .storeId(storeId)
            .name(DEFAULT_STORE_NAME)
            .introduce(DEFAULT_INTRODUCE)
            .profile(DEFAULT_PROFILE)
            .status(StoreApplicationStatus.PENDING)
            .phoneNumber(DEFAULT_PHONE)
            .subPhoneNumber(DEFAULT_SUBPHONE)
            .email(DEFAULT_EMAIL)
            .originAddress(DEFAULT_ADDRESS)
            .originAddressDetail(DEFAULT_DETAIL_ADDRESS)
            .createdAt(LocalDateTime.now())
            .modifiedAt(LocalDateTime.now())
            .build();
    }

    public static StoreApplicationDetail defaultStoreApplicationDetail(
        Long storeId,
        StoreApplicationStatus status
    ) {
        return StoreApplicationDetail.builder()
            .storeApplicationId(1L)
            .sellerId(1L)
            .storeId(storeId)
            .name(DEFAULT_STORE_NAME)
            .introduce(DEFAULT_INTRODUCE)
            .profile(DEFAULT_PROFILE)
            .status(status)
            .phoneNumber(DEFAULT_PHONE)
            .subPhoneNumber(DEFAULT_SUBPHONE)
            .email(DEFAULT_EMAIL)
            .originAddress(DEFAULT_ADDRESS)
            .originAddressDetail(DEFAULT_DETAIL_ADDRESS)
            .createdAt(LocalDateTime.now())
            .modifiedAt(LocalDateTime.now())
            .build();
    }
}
