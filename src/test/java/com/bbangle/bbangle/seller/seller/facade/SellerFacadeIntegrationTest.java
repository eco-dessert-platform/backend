package com.bbangle.bbangle.seller.seller.facade;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.config.S3IntegrationTestSupport;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerResponse;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] SellerFacadeUnitTest")
@Transactional
class SellerFacadeIntegrationTest extends S3IntegrationTestSupport {

    @Autowired
    SellerFacade sellerFacade;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Nested
    @DisplayName("getRegisteredStoreDetail() 테스트")
    class GetRegisteredStoreDetailTest {

        private Seller saveNewSeller(Store store) {
            Seller seller = SellerFixture.defaultSeller(store);
            return sellerRepository.saveAndFlush(seller);
        }

        @Test
        @DisplayName("등록 신청한 스토어가 존재할 경우 해당 스토어 정보를 조회한다.")
        void getRegisteredStoreDetail_exist_registeredStore() {

            // given
            Store store = storeRepository.saveAndFlush(StoreFixture.defaultStore(StoreStatus.RESERVED));
            Seller seller = saveNewSeller(store);

            // when
            SellerResponse.RegisteredStoreDetail result = sellerFacade.getRegisteredStoreDetail(seller.getId());

            // then
            assertThat(result.sellerId()).isEqualTo(seller.getId());
            assertThat(result.store().storeId()).isEqualTo(store.getId());
            assertThat(result.store().name()).isEqualTo(store.getName());
        }

        @Test
        @DisplayName("등록 신청한 스토어가 없을 경우 null을 반환한다.")
        void getRegisteredStoreDetail_noExist_registeredStore() {

            // given
            Seller seller = saveNewSeller(null);

            // when
            SellerResponse.RegisteredStoreDetail result = sellerFacade.getRegisteredStoreDetail(seller.getId());

            // then
            assertThat(result.sellerId()).isEqualTo(seller.getId());
            assertThat(result.store()).isNull();
        }
    }
}