package com.bbangle.bbangle.store.seller.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] SellerStoreService - Store 등록")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SellerStoreServiceRegisterIntegrationTest {

    @Autowired
    private SellerStoreService sellerStoreService;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("스토어 등록 시 이미 존재하는 스토어면 상세 정보를 수정한다.")
    void success_createStore_with_storeId() {

        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());
        StoreRequest.StoreCreateRequest request = new StoreRequest.StoreCreateRequest(
            "store",
            "newProfile.png",
            "newIntroduce",
            "01011112222",
            "01099998888",
            "temp@test.com",
            "서울",
            "123동",
            store.getId()
        );

        // when
        Store result = sellerStoreService.createStore(request, null);

        // then
        assertThat(result.getId()).isEqualTo(store.getId());
        assertThat(result.getName()).isEqualTo(store.getName());
        assertThat(result.getProfile()).isEqualTo("newProfile.png");
        assertThat(result.getIntroduce()).isEqualTo("newIntroduce");
        assertThat(result.getPhoneNumberVO().getPhoneNumber()).isEqualTo("01011112222");
        assertThat(result.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo("01099998888");
        assertThat(result.getEmailVO().getEmail()).isEqualTo("temp@test.com");
        assertThat(result.getOriginAddressLine()).isEqualTo("서울");
        assertThat(result.getOriginAddressDetail()).isEqualTo("123동");
    }

    @Test
    @DisplayName("존재하는 storeId로 조회하면 예외를 던진다.")
    void fail_createStore_nofFound() {

        // given
        StoreRequest.StoreCreateRequest request = new StoreRequest.StoreCreateRequest(
            "store",
            "newProfile.png",
            "newIntroduce",
            "01011112222",
            "01099998888",
            "temp@test.com",
            "서울",
            "123동",
            999L
        );

        // when & then
        Assertions.assertThatThrownBy(() -> sellerStoreService.createStore(request, null)
            )
            .isInstanceOf(BbangleException.class)
            .satisfies(e -> {
                BbangleException ex = (BbangleException) e;
                Assertions.assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.STORE_NOT_FOUND);
            });
    }

    @Test
    @DisplayName("새로운 스토어를 생성한다.")
    void success_createStore_createStore() {

        // given
        StoreRequest.StoreCreateRequest request = new StoreRequest.StoreCreateRequest(
            "store",
            null,
            "newIntroduce",
            "01011112222",
            "01099998888",
            "temp@test.com",
            "서울",
            "123동",
            null
        );
        String profileImage = "profile.jpg";

        // when
        Store result = sellerStoreService.createStore(request, profileImage);

        // then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("store");
        assertThat(result.getProfile()).isEqualTo("profile.jpg");
        assertThat(result.getIntroduce()).isEqualTo("newIntroduce");
        assertThat(result.getPhoneNumberVO().getPhoneNumber()).isEqualTo("01011112222");
        assertThat(result.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo("01099998888");
        assertThat(result.getEmailVO().getEmail()).isEqualTo("temp@test.com");
        assertThat(result.getOriginAddressLine()).isEqualTo("서울");
        assertThat(result.getOriginAddressDetail()).isEqualTo("123동");
    }

    @Test
    @DisplayName("신규 스토어 생성 시 프로필 이미지가 없으면 실패.")
    void fail_createStore_without_profile() {

        // given
        StoreRequest.StoreCreateRequest request = new StoreRequest.StoreCreateRequest(
            "store",
            null,
            "newIntroduce",
            "01011112222",
            "01099998888",
            "temp@test.com",
            "서울",
            "123동",
            null
        );

        // when & then
        Assertions.assertThatThrownBy(() -> sellerStoreService.createStore(request, null)
            )
            .isInstanceOf(BbangleException.class)
            .satisfies(e -> {
                BbangleException ex = (BbangleException) e;
                Assertions.assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_PROFILE);
            });
    }

    @Test
    void success_register_store() {

        // given
        Seller seller = SellerFixture.defaultSeller();
        Store store = StoreFixture.defaultStore();

        // when
        sellerStoreService.registerStore(seller, store);

        // then
        assertThat(seller.getStore()).isEqualTo(store);
        assertThat(seller.getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
        assertThat(store.getStatus()).isEqualTo(StoreStatus.RESERVED);
    }
}