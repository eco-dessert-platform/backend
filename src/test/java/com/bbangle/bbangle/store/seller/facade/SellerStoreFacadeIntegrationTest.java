package com.bbangle.bbangle.store.seller.facade;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SellerStoreDTO;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.StoreNameCheck;
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
            SellerStoreDTO result = sellerStoreFacade.getRegisteredStoreDetail(seller.getId());

            // then
            assertThat(result.sellerId()).isEqualTo(seller.getId());
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
}