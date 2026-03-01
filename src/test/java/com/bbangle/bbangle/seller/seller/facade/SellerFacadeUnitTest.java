package com.bbangle.bbangle.seller.seller.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerResponse;
import com.bbangle.bbangle.seller.seller.service.AccountVerificationService;
import com.bbangle.bbangle.seller.seller.service.SellerDocumentService;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.mapper.SellerStoreMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위테스트] SellerFacade")
@ExtendWith(MockitoExtension.class)
class SellerFacadeUnitTest {

    @InjectMocks
    SellerFacade sellerFacade;

    @Mock
    S3Service s3Service;

    @Mock
    SellerDocumentService sellerDocumentService;

    @Mock
    AccountVerificationService accountVerificationService;

    @Mock
    SellerService sellerService;

    @Mock
    SellerStoreMapper sellerStoreMapper;

    @Nested
    @DisplayName("getRegisteredStoreDetail() 테스트")
    class GetRegisteredStoreDetailTest {

        @Test
        @DisplayName("등록 신청한 스토어가 존재할 경우 해당 스토어 정보를 조회한다.")
        void getRegisteredStoreDetail_exist_registeredStore() {

            // given
            Long sellerId = 1L;

            Store store = StoreFixture.defaultStore();
            Seller seller = SellerFixture.withId(
                SellerFixture.defaultSeller(store),
                sellerId
            );
            StoreResponse.SellerStoreDetail storeDetail = mock(StoreResponse.SellerStoreDetail.class);

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(sellerStoreMapper.toSellerStoreDetail(store)).willReturn(storeDetail);

            // when
            SellerResponse.RegisteredStoreDetail result = sellerFacade.getRegisteredStoreDetail(sellerId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.sellerId()).isEqualTo(sellerId);
            assertThat(result.store()).isEqualTo(storeDetail);
        }

        @Test
        @DisplayName("등록 신청한 스토어가 없을 경우 null을 반환한다.")
        void getRegisteredStoreDetail_noExist_registeredStore() {

            // given
            Long sellerId = 1L;
            Seller seller = SellerFixture.withId(
                SellerFixture.defaultSeller(CertificationStatus.NEW),
                sellerId
            );

            given(sellerService.getSellerById(sellerId)).willReturn(seller);

            // when
            SellerResponse.RegisteredStoreDetail result = sellerFacade.getRegisteredStoreDetail(sellerId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.sellerId()).isEqualTo(sellerId);
            assertThat(result.store()).isNull();
            verify(sellerStoreMapper, never()).toSellerStoreDetail(any());
        }
    }
}