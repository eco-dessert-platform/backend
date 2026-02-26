package com.bbangle.bbangle.store.seller.facade;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SellerStoreDetail;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.StoreNameCheck;
import com.bbangle.bbangle.store.seller.controller.mapper.SellerStoreMapper;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위테스트] SellerStoreFacade")
@ExtendWith(MockitoExtension.class)
class SellerStoreFacadeUnitTest {

    @InjectMocks
    SellerStoreFacade sellerStoreFacade;

    @Mock
    SellerStoreService sellerStoreService;

    @Mock
    SellerService sellerService;

    @Mock
    SellerStoreMapper sellerStoreMapper;

    @Nested
    @DisplayName("checkStoreName() 테스트")
    class checkStoreNameTest {

        @Test
        @DisplayName("Store가 존재하지 않으면 등록 가능하다.")
        void notExist() {

            // given
            String storeName = "notExistStore";

            given(sellerStoreService.findStoreByStoreName(storeName)).willReturn(Optional.empty());

            // when
            StoreNameCheck result = sellerStoreFacade.checkStoreName(storeName);

            // then
            assertThat(result.available()).isTrue();
            assertThat(result.store()).isNull();

            verify(sellerService, never()).existsSellerByStoreId(any());
        }

        @Test
        @DisplayName("Store가 존재하고 해당 Store를 등록한 Seller가 없으면 등록 가능하다.")
        void exist_store_notExist_seller() {

            // given
            Store store = mock(Store.class);
            SellerStoreDetail sellerStoreDetail = mock(SellerStoreDetail.class);

            given(store.getId()).willReturn(1L);
            given(sellerStoreService.findStoreByStoreName(DEFAULT_STORE_NAME)).willReturn(Optional.of(store));
            given(sellerService.existsSellerByStoreId(1L)).willReturn(false);
            given(sellerStoreMapper.toSellerStoreDetail(store)).willReturn(sellerStoreDetail);

            // when
            StoreNameCheck result = sellerStoreFacade.checkStoreName(DEFAULT_STORE_NAME);

            // then
            assertThat(result.available()).isTrue();
            assertThat(result.store()).isEqualTo(sellerStoreDetail);
        }

        @Test
        @DisplayName("Store가 존재하고 해당 Store를 등록한 Seller가 존재하면 등록 불가능하다.")
        void exist_store_exist_seller() {

            // given
            Store store = mock(Store.class);
            SellerStoreDetail sellerStoreDetail = mock(SellerStoreDetail.class);

            given(store.getId()).willReturn(1L);
            given(sellerStoreService.findStoreByStoreName(DEFAULT_STORE_NAME)).willReturn(Optional.of(store));
            given(sellerService.existsSellerByStoreId(1L)).willReturn(true);
            given(sellerStoreMapper.toSellerStoreDetail(store)).willReturn(sellerStoreDetail);

            // when
            StoreNameCheck result = sellerStoreFacade.checkStoreName(DEFAULT_STORE_NAME);

            // then
            assertThat(result.available()).isFalse();
            assertThat(result.store()).isEqualTo(sellerStoreDetail);
        }
    }
}