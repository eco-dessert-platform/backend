package com.bbangle.bbangle.store.domain;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_DETAIL_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_EMAIL;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_INTRODUCE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PROFILE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_SUBPHONE;
import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.model.StoreApplicationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] StoreApplication")
class StoreApplicationTest {

    @Test
    @DisplayName("스토어 등록 신청 객체 생성에 성공한다.")
    void success_create_storeApplication() {

        // given & when
        Seller seller = SellerFixture.withId(SellerFixture.defaultSeller(), 1L);
        Store store = StoreFixture.defaultStore();
        StoreApplication storeApplication = StoreApplication.createStoreApplication(
            DEFAULT_NAME, DEFAULT_PROFILE,
            DEFAULT_INTRODUCE,
            DEFAULT_PHONE, DEFAULT_SUBPHONE,
            DEFAULT_EMAIL,
            DEFAULT_ADDRESS, DEFAULT_DETAIL_ADDRESS,
            seller, store
        );

        // then
        assertThat(storeApplication).isNotNull();
        assertThat(storeApplication.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(storeApplication.getProfile()).isEqualTo(DEFAULT_PROFILE);
        assertThat(storeApplication.getIntroduce()).isEqualTo(DEFAULT_INTRODUCE);
        assertThat(storeApplication.getStatus()).isEqualTo(StoreApplicationStatus.PENDING);
        assertThat(storeApplication.getPhoneNumberVO().getPhoneNumber()).isEqualTo(DEFAULT_PHONE);
        assertThat(storeApplication.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(DEFAULT_SUBPHONE);
        assertThat(storeApplication.getEmailVO().getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(storeApplication.getOriginAddressLine()).isEqualTo(DEFAULT_ADDRESS);
        assertThat(storeApplication.getOriginAddressDetail()).isEqualTo(DEFAULT_DETAIL_ADDRESS);
        assertThat(storeApplication.getSeller()).isEqualTo(seller);
        assertThat(storeApplication.getStore()).isEqualTo(store);
    }
}