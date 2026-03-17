package com.bbangle.bbangle.store.seller.facade;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.config.S3IntegrationTestSupport;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture;
import com.bbangle.bbangle.fixture.store.seller.controller.dto.SellerStoreRequestFixture;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.repository.StoreNameRequestRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest.UpdateStoreDetailRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest.UpdateStoreNameRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SellerStoreDTO;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.StoreNameCheck;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.UpdateStoreNameResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] SellerStoreFacade")
@Transactional
class SellerStoreFacadeIntegrationTest extends S3IntegrationTestSupport {

    @Autowired
    private SellerStoreFacade sellerStoreFacade;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreNameRequestRepository storeNameRequestRepository;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("checkStoreName() 테스트")
    class checkStoreNameTest {

        @Test
        @DisplayName("Store가 존재하지 않으면 등록 가능하다.")
        void notExist() {

            // given
            String storeName = "notExistStore";

            // when
            StoreNameCheck result = sellerStoreFacade.checkStoreName(storeName);

            // then
            assertThat(result.available()).isTrue();
            assertThat(result.store()).isNull();
        }

        @Test
        @DisplayName("Store가 존재하고 해당 Store를 등록한 Seller가 없으면 등록 가능하다.")
        void exist_store_notExist_seller() {

            // given
            Store store = storeRepository.saveAndFlush(StoreFixture.defaultStore());

            // when
            StoreNameCheck result = sellerStoreFacade.checkStoreName(DEFAULT_STORE_NAME);

            // then
            assertThat(result.available()).isTrue();
            assertThat(result.store()).isNotNull();
            assertThat(result.store().storeId()).isEqualTo(store.getId());
            assertThat(result.store().name()).isEqualTo(DEFAULT_STORE_NAME);
        }

        @Test
        @DisplayName("Store가 존재하고 해당 Store를 등록한 Seller가 존재하면 등록 불가능하다.")
        void exist_store_exist_seller() {

            // given
            Store store = storeRepository.saveAndFlush(StoreFixture.defaultStore());
            sellerRepository.saveAndFlush(SellerFixture.defaultSeller(store));

            // when
            StoreNameCheck result = sellerStoreFacade.checkStoreName(DEFAULT_STORE_NAME);

            // then
            assertThat(result.available()).isFalse();
            assertThat(result.store()).isNotNull();
            assertThat(result.store().storeId()).isEqualTo(store.getId());
            assertThat(result.store().name()).isEqualTo(DEFAULT_STORE_NAME);
        }
    }

    @Nested
    @DisplayName("getRegisteredStoreDetail() 테스트")
    class GetRegisteredStoreDetailTest {

        private Seller saveNewSeller(Store store) {
            Seller seller = SellerFixture.defaultSeller(store);
            return sellerRepository.saveAndFlush(seller);
        }

        @Test
        @DisplayName("등록한 스토어가 존재할 경우 해당 스토어 정보를 조회한다.")
        void success_getRegisteredStoreDetail() {

            // given
            Store store = storeRepository.saveAndFlush(StoreFixture.defaultStore());
            Seller seller = saveNewSeller(store);

            // when
            StoreNameCheck result = sellerStoreFacade.getRegisteredStoreDetail(seller.getId());

            // then
            assertThat(result.available()).isTrue();
            assertThat(result.store().storeId()).isEqualTo(store.getId());
            assertThat(result.store().name()).isEqualTo(store.getName());
        }

        @ParameterizedTest
        @EnumSource(
            value = StoreApprovalStatus.class,
            names = {"APPROVE", "PENDING"}
        )
        @DisplayName("스토어명 변경 요청이 APPROVE 또는 PENDING이면 스토어명 변경 불가능하다.")
        void getRegisteredStoreDetail_updateStoreName(StoreApprovalStatus status) {

            // given
            Store store = storeRepository.saveAndFlush(StoreFixture.defaultStore());
            Seller seller = saveNewSeller(store);
            storeNameRequestRepository.saveAndFlush(
                StoreNameRequestFixture.defaultStoreNameRequest(seller, store, status)
            );

            // when
            StoreNameCheck result = sellerStoreFacade.getRegisteredStoreDetail(seller.getId());

            // then
            assertThat(result.available()).isFalse();
            assertThat(result.store().storeId()).isEqualTo(store.getId());
            assertThat(result.store().name()).isEqualTo(store.getName());
        }

        @Test
        @DisplayName("등록한 스토어가 없을 경우 조회에 실패한다.")
        void fail_getRegisteredStoreDetail() {

            // given
            Seller seller = saveNewSeller(null);

            // when & then
            assertThatThrownBy(() -> sellerStoreFacade.getRegisteredStoreDetail(seller.getId()))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.NOT_REGISTERED_STORE);
                });
        }
    }

    @Nested
    @DisplayName("updateStoreName() 테스트")
    class UpdateStoreNameTest {

        @Test
        @DisplayName("스토어명 변경 신청에 성공한다.")
        void success_updateStoreName() {

            // given
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller(store));
            UpdateStoreNameRequest request = SellerStoreRequestFixture.defaultUpdateStoreNameRequest();

            // when
            UpdateStoreNameResponse response = sellerStoreFacade.updateStoreName(seller.getId(), request);

            em.flush();
            em.clear();

            // then
            assertThat(response).isNotNull();

            StoreNameRequest saved = storeNameRequestRepository.findById(response.storeNameRequestId()).orElseThrow();
            assertThat(saved.getSeller().getId()).isEqualTo(seller.getId());
            assertThat(saved.getStore().getId()).isEqualTo(store.getId());
            assertThat(saved.getCurrentName()).isEqualTo(store.getName());
            assertThat(saved.getNewName()).isEqualTo(request.newName());
            assertThat(saved.getStatus()).isEqualTo(StoreApprovalStatus.PENDING);
            assertThat(saved.getRejectCategory()).isNull();
            assertThat(saved.getRejectDetail()).isNull();
        }

        @Test
        @DisplayName("이미 승인된 신청이 존재하면 신청에 실패한다.")
        void fail_updateStoreName_already_approved() {

            // given
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller(store));
            UpdateStoreNameRequest request = SellerStoreRequestFixture.defaultUpdateStoreNameRequest();

            storeNameRequestRepository.save(
                StoreNameRequestFixture.defaultStoreNameRequest(seller, store, StoreApprovalStatus.APPROVE)
            );

            // when & then
            assertThatThrownBy(() -> sellerStoreFacade.updateStoreName(
                seller.getId(), request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ALREADY_UPDATE_STORE_NAME);
                });
        }

        @Test
        @DisplayName("이미 대기 중인 신청이 존재하면 신청에 실패한다.")
        void fail_updateStoreName_exists_pending() {

            // given
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller(store));
            UpdateStoreNameRequest request = SellerStoreRequestFixture.defaultUpdateStoreNameRequest();

            storeNameRequestRepository.save(
                StoreNameRequestFixture.defaultStoreNameRequest(seller, store, StoreApprovalStatus.PENDING)
            );

            // when & then
            assertThatThrownBy(() -> sellerStoreFacade.updateStoreName(
                seller.getId(), request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.REQUEST_IS_PENDING);
                });
        }

        @Test
        @DisplayName("이미 사용중인 스토어명일 경우 신청에 실패한다.")
        void fail_updateStoreName_duplicate_storeName() {

            // given
            String duplicatedStoreName = "duplicatedStoreName";
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller(store));
            UpdateStoreNameRequest request = SellerStoreRequestFixture.defaultUpdateStoreNameRequest(duplicatedStoreName);

            storeRepository.save(StoreFixture.defaultStore(duplicatedStoreName));
            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> sellerStoreFacade.updateStoreName(
                seller.getId(), request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ALREADY_RESERVED_STORE);
                });
        }
    }

    @Nested
    @DisplayName("updateStoreDetail() 테스트")
    class UpdateStoreDetailTest {

        private MockMultipartFile mockProfileImage() {
            return new MockMultipartFile(
                "profileImage",
                "test-image.png",
                "image/png",
                "fake image content".getBytes()
            );
        }

        private Seller saveNewSeller() {
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller(store));
            em.flush();
            em.clear();

            return seller;
        }

        @Test
        @DisplayName("유효한 정보가 주어지는 경우 스토어 상세 정보 변경에 성공한다.")
        void success_updateStoreDetail() {

            // given
            Seller seller = saveNewSeller();
            UpdateStoreDetailRequest request = SellerStoreRequestFixture.defaultUpdateStoreDetailRequest();
            MockMultipartFile mockFile = mockProfileImage();

            // when
            sellerStoreFacade.updateStoreDetail(seller.getId(), request, mockFile);
            em.flush();
            em.clear();

            // then
            Store updatedStore = storeRepository.findById(seller.getStore().getId()).orElseThrow();

            assertThat(updatedStore.getProfile()).isNotEqualTo(seller.getStore().getProfile());
            assertThat(updatedStore.getIntroduce()).isEqualTo(request.introduce());
            assertThat(updatedStore.getPhoneNumberVO().getPhoneNumber()).isEqualTo(request.phoneNumber());
            assertThat(updatedStore.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(request.subPhoneNumber());
            assertThat(updatedStore.getEmailVO().getEmail()).isEqualTo(request.email());
            assertThat(updatedStore.getOriginAddressLine()).isEqualTo(request.originAddress());
            assertThat(updatedStore.getOriginAddressDetail()).isEqualTo(request.originAddressDetail());
        }

        @Test
        @DisplayName("업로드한 사진이 없을 경우 기존 프로필을 유지한다.")
        void success_updateStoreDetail_withoutImage() {

            // given
            Seller seller = saveNewSeller();
            UpdateStoreDetailRequest request = SellerStoreRequestFixture.defaultUpdateStoreDetailRequest();

            // when
            sellerStoreFacade.updateStoreDetail(seller.getId(), request, null);
            em.flush();
            em.clear();

            // then
            Store updatedStore = storeRepository.findById(seller.getStore().getId()).orElseThrow();

            assertThat(updatedStore.getProfile()).isEqualTo(seller.getStore().getProfile());
            assertThat(updatedStore.getIntroduce()).isEqualTo(request.introduce());
            assertThat(updatedStore.getPhoneNumberVO().getPhoneNumber()).isEqualTo(request.phoneNumber());
            assertThat(updatedStore.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(request.subPhoneNumber());
            assertThat(updatedStore.getEmailVO().getEmail()).isEqualTo(request.email());
            assertThat(updatedStore.getOriginAddressLine()).isEqualTo(request.originAddress());
            assertThat(updatedStore.getOriginAddressDetail()).isEqualTo(request.originAddressDetail());
        }

        @Test
        @DisplayName("스토어를 등록하지 않은 판매자 계정일 경우 예외를 던진다.")
        void fail_updateStoreDetail_storeNotFound() {

            // given
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller());
            em.flush();
            em.clear();

            UpdateStoreDetailRequest request = SellerStoreRequestFixture.defaultUpdateStoreDetailRequest();
            MockMultipartFile mockFile = mockProfileImage();

            // when & then
            assertThatThrownBy(() -> sellerStoreFacade.updateStoreDetail(seller.getId(), request, mockFile))
                .isInstanceOf(BbangleException.class)
                .satisfies(ex -> {
                    BbangleException e = (BbangleException) ex;
                    assertThat(e.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.STORE_NOT_FOUND);
                });
        }
    }
}