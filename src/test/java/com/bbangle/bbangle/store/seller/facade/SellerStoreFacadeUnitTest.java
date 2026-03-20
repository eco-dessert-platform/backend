package com.bbangle.bbangle.store.seller.facade;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture;
import com.bbangle.bbangle.fixture.store.seller.controller.dto.SellerStoreRequestFixture;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest.UpdateStoreDetailRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest.UpdateStoreNameRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SellerStoreDTO;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SellerStoreDetail;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.StoreNameCheck;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.UpdateStoreNameResponse;
import com.bbangle.bbangle.store.seller.controller.mapper.SellerStoreMapper;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

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

    @Mock
    S3Service s3Service;

    @Mock
    MultipartFile multipartFile;

    @Nested
    @DisplayName("checkStoreName() 테스트")
    class CheckStoreNameTest {

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
            SellerStoreDTO result = sellerStoreFacade.getRegisteredStoreDetail(sellerId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.sellerId()).isEqualTo(sellerId);
            assertThat(result.store()).isEqualTo(storeDetail);
        }

        @Test
        @DisplayName("등록한 스토어가 없을 경우 조회에 실패한다.")
        void fail_getRegisteredStoreDetail() {

            // given
            Long sellerId = 1L;
            Seller seller = SellerFixture.withId(
                SellerFixture.defaultSeller(CertificationStatus.NEW),
                sellerId
            );

            given(sellerService.getSellerById(sellerId)).willReturn(seller);

            // when & then
            assertThatThrownBy(() -> sellerStoreFacade.getRegisteredStoreDetail(sellerId))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.NOT_REGISTERED_STORE);
                });

            verify(sellerStoreMapper, never()).toSellerStoreDetail(any());
        }
    }

    @Nested
    @DisplayName("updateStoreName() 테스트")
    class UpdateStoreNameTest {

        @Test
        @DisplayName("스토어명 변경 신청에 성공한다.")
        void success_updateStoreName() {

            // given
            Long sellerId = 1L;
            Store store = StoreFixture.defaultStore();
            Seller seller = SellerFixture.defaultSeller(store);
            StoreNameRequest storeNameRequest = StoreNameRequestFixture.defaultStoreNameRequest(seller, store);
            UpdateStoreNameRequest request = SellerStoreRequestFixture.defaultUpdateStoreNameRequest();
            UpdateStoreNameResponse response = mock(UpdateStoreNameResponse.class);

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(sellerStoreService.existsByStatusAndSellerId(seller, StoreApprovalStatus.APPROVE)).willReturn(false);
            given(sellerStoreService.existsByStatusAndSellerId(seller, StoreApprovalStatus.PENDING)).willReturn(false);
            given(sellerStoreService.findStoreByStoreName(request.newName())).willReturn(Optional.empty());
            given(sellerStoreMapper.toUpdateStoreNameResponse(storeNameRequest)).willReturn(response);
            given(sellerStoreService.updateStoreName(request, seller)).willReturn(storeNameRequest);

            // when
            UpdateStoreNameResponse result = sellerStoreFacade.updateStoreName(sellerId, request);

            // then
            assertThat(result).isEqualTo(response);

            verify(sellerStoreService).updateStoreName(request, seller);
            verify(sellerStoreMapper).toUpdateStoreNameResponse(storeNameRequest);

            InOrder inOrder = inOrder(sellerStoreService);
            inOrder.verify(sellerStoreService).existsByStatusAndSellerId(seller, StoreApprovalStatus.APPROVE);
            inOrder.verify(sellerStoreService).existsByStatusAndSellerId(seller, StoreApprovalStatus.PENDING);
            inOrder.verify(sellerStoreService).findStoreByStoreName(request.newName());
            inOrder.verify(sellerStoreService).updateStoreName(request, seller);
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("이미 승인된 신청이 존재하면 신청에 실패한다.")
        void fail_updateStoreName_already_approved() {

            // given
            Long sellerId = 1L;
            Store store = StoreFixture.defaultStore();
            Seller seller = SellerFixture.defaultSeller(store);
            UpdateStoreNameRequest request = SellerStoreRequestFixture.defaultUpdateStoreNameRequest();

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(sellerStoreService.existsByStatusAndSellerId(seller, StoreApprovalStatus.APPROVE)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerStoreFacade.updateStoreName(sellerId, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ALREADY_UPDATE_STORE_NAME);
                });

            verify(sellerStoreService).existsByStatusAndSellerId(seller, StoreApprovalStatus.APPROVE);
            verify(sellerStoreService, never()).existsByStatusAndSellerId(seller, StoreApprovalStatus.PENDING);
            verify(sellerStoreService, never()).findStoreByStoreName(any());
            verify(sellerStoreMapper, never()).toUpdateStoreNameResponse(any());
        }

        @Test
        @DisplayName("이미 대기 중인 신청이 존재하면 신청에 실패한다.")
        void fail_updateStoreName_exists_pending() {

            // given
            Long sellerId = 1L;
            Store store = StoreFixture.defaultStore();
            Seller seller = SellerFixture.defaultSeller(store);
            UpdateStoreNameRequest request = SellerStoreRequestFixture.defaultUpdateStoreNameRequest();

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(sellerStoreService.existsByStatusAndSellerId(seller, StoreApprovalStatus.APPROVE)).willReturn(false);
            given(sellerStoreService.existsByStatusAndSellerId(seller, StoreApprovalStatus.PENDING)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> sellerStoreFacade.updateStoreName(sellerId, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.REQUEST_IS_PENDING);
                });

            verify(sellerStoreService).existsByStatusAndSellerId(seller, StoreApprovalStatus.APPROVE);
            verify(sellerStoreService).existsByStatusAndSellerId(seller, StoreApprovalStatus.PENDING);
            verify(sellerStoreService, never()).findStoreByStoreName(any());
            verify(sellerStoreMapper, never()).toUpdateStoreNameResponse(any());
        }

        @Test
        @DisplayName("이미 사용중인 스토어명일 경우 신청에 실패한다.")
        void fail_updateStoreName_duplicate_storeName() {

            // given
            Long sellerId = 1L;
            Store store = StoreFixture.defaultStore();
            Seller seller = SellerFixture.defaultSeller(store);
            UpdateStoreNameRequest request = SellerStoreRequestFixture.defaultUpdateStoreNameRequest();

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(sellerStoreService.existsByStatusAndSellerId(seller, StoreApprovalStatus.APPROVE)).willReturn(false);
            given(sellerStoreService.existsByStatusAndSellerId(seller, StoreApprovalStatus.PENDING)).willReturn(false);
            given(sellerStoreService.findStoreByStoreName(request.newName())).willReturn(Optional.of(mock(Store.class)));

            // when & then
            assertThatThrownBy(() -> sellerStoreFacade.updateStoreName(sellerId, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ALREADY_RESERVED_STORE);
                });

            verify(sellerStoreService).existsByStatusAndSellerId(seller, StoreApprovalStatus.APPROVE);
            verify(sellerStoreService).existsByStatusAndSellerId(seller, StoreApprovalStatus.PENDING);
            verify(sellerStoreService).findStoreByStoreName(request.newName());
            verify(sellerStoreMapper, never()).toUpdateStoreNameResponse(any());
        }
    }

    @Nested
    @DisplayName("updateStoreDetail() 테스트")
    class UpdateStoreDetailTest {

        @Test
        @DisplayName("유효한 정보가 주어지는 경우 스토어 상세 정보 변경에 성공한다.")
        void success_updateStoreDetail() {

            // given
            Long sellerId = 1L;
            Store store = mock(Store.class);
            Seller seller = mock(Seller.class);
            UpdateStoreDetailRequest request = mock(UpdateStoreDetailRequest.class);
            String profilePath = "s3/profile.png";

            SellerStoreDetail sellerStoreDetail = mock(SellerStoreDetail.class);

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(seller.getId()).willReturn(sellerId);
            given(seller.getStore()).willReturn(store);
            given(s3Service.saveAndReturnWithCdn(anyString(), eq(multipartFile))).willReturn(profilePath);
            given(sellerStoreService.updateStoreDetail(request, profilePath, store)).willReturn(store);
            given(sellerStoreMapper.toSellerStoreDetail(store)).willReturn(sellerStoreDetail);

            // when
            SellerStoreDetail result = sellerStoreFacade.updateStoreDetail(sellerId, request, multipartFile);

            // then
            assertThat(result).isEqualTo(sellerStoreDetail);
            verify(s3Service).saveAndReturnWithCdn(anyString(), eq(multipartFile));
            verify(sellerStoreService).updateStoreDetail(request, profilePath, store);
            verify(sellerStoreMapper).toSellerStoreDetail(store);
            verify(s3Service, never()).deleteImage(any());
        }

        @Test
        @DisplayName("업로드한 사진이 없을 경우 기존 프로필을 유지한다.")
        void success_updateStoreDetail_withoutImage() {

            // given
            Long sellerId = 1L;
            Store store = mock(Store.class);
            Seller seller = mock(Seller.class);
            UpdateStoreDetailRequest request = mock(UpdateStoreDetailRequest.class);

            SellerStoreDetail sellerStoreDetail = mock(SellerStoreDetail.class);

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(seller.getStore()).willReturn(store);
            given(sellerStoreService.updateStoreDetail(request, null, store)).willReturn(store);
            given(sellerStoreMapper.toSellerStoreDetail(store)).willReturn(sellerStoreDetail);

            // when
            SellerStoreDetail result = sellerStoreFacade.updateStoreDetail(sellerId, request, null);

            // then
            assertThat(result).isEqualTo(sellerStoreDetail);
            verify(sellerStoreService).updateStoreDetail(request, null, store);
            verify(sellerStoreMapper).toSellerStoreDetail(store);
            verify(s3Service, never()).saveAndReturnWithCdn(any(), any());
            verify(s3Service, never()).deleteImage(any());
        }

        @Test
        @DisplayName("스토어를 등록하지 않은 판매자 계정일 경우 예외를 던진다.")
        void fail_updateStoreDetail_storeNotFound() {

            // given
            Long sellerId = 1L;
            Seller seller = mock(Seller.class);
            UpdateStoreDetailRequest request = mock(UpdateStoreDetailRequest.class);

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(seller.getStore()).willReturn(null);

            // when & then
            assertThatThrownBy(() -> sellerStoreFacade.updateStoreDetail(sellerId, request, multipartFile))
                .isInstanceOf(BbangleException.class)
                .satisfies(ex -> {
                    BbangleException e = (BbangleException) ex;
                    assertThat(e.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.STORE_NOT_FOUND);
                });
            verifyNoInteractions(s3Service);
        }

        @Test
        @DisplayName("스토어 상세 정보 수정 중 BbangleException 예외가 발생하면 S3 롤백 한다.")
        void fail_rollback_s3_by_bbangleException() {

            // given
            Long sellerId = 1L;
            Store store = mock(Store.class);
            Seller seller = mock(Seller.class);
            UpdateStoreDetailRequest request = mock(UpdateStoreDetailRequest.class);
            String profilePath = "s3/profile.png";

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(seller.getId()).willReturn(sellerId);
            given(seller.getStore()).willReturn(store);
            given(s3Service.saveAndReturnWithCdn(anyString(), any())).willReturn(profilePath);
            given(sellerStoreService.updateStoreDetail(any(), any(), any()))
                .willThrow(new BbangleException(BbangleErrorCode._BAD_REQUEST));

            // when & then
            assertThatThrownBy(() -> sellerStoreFacade.updateStoreDetail(sellerId, request, multipartFile))
                .isInstanceOf(BbangleException.class)
                .satisfies(ex -> {
                    BbangleException e = (BbangleException) ex;
                    assertThat(e.getBbangleErrorCode()).isEqualTo(BbangleErrorCode._BAD_REQUEST);
                });
            verify(s3Service).deleteImage(profilePath);
        }

        @Test
        @DisplayName("스토어 상세 정보 수정 중 Exception 예외가 발생하면 S3 롤백 한다.")
        void fail_rollback_s3_by_exception() {

            // given
            Long sellerId = 1L;
            Store store = mock(Store.class);
            Seller seller = mock(Seller.class);
            UpdateStoreDetailRequest request = mock(UpdateStoreDetailRequest.class);
            String profilePath = "s3/profile.png";

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(seller.getId()).willReturn(sellerId);
            given(seller.getStore()).willReturn(store);
            given(s3Service.saveAndReturnWithCdn(anyString(), any())).willReturn(profilePath);
            given(sellerStoreService.updateStoreDetail(any(), any(), any()))
                .willThrow(new RuntimeException());

            // when & then
            assertThatThrownBy(() -> sellerStoreFacade.updateStoreDetail(sellerId, request, multipartFile))
                .isInstanceOf(BbangleException.class)
                .satisfies(ex -> {
                    BbangleException e = (BbangleException) ex;
                    assertThat(e.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.STORE_UPDATE_FAILED);
                });
            verify(s3Service).deleteImage(profilePath);
        }
    }
}