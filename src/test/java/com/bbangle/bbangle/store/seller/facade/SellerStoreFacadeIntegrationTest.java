package com.bbangle.bbangle.store.seller.facade;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture;
import com.bbangle.bbangle.fixture.store.seller.controller.dto.SellerStoreRequestFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.repository.StoreNameRequestRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest.UpdateStoreNameRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.StoreNameCheck;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.UpdateStoreNameResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] SellerStoreFacade")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SellerStoreFacadeIntegrationTest {

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
            assertThat(saved.getRejectReason()).isNull();
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
}