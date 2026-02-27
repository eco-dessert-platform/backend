package com.bbangle.bbangle.store.seller.facade;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PROFILE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationRequest.StoreApplicationCreateRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationResponse.StoreApplicationDetail;
import com.bbangle.bbangle.store.seller.controller.mapper.SellerStoreApplicationMapper;
import com.bbangle.bbangle.store.seller.service.SellerStoreApplicationService;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@DisplayName("[단위테스트] SellerStoreApplicationFacade")
@ExtendWith(MockitoExtension.class)
class SellerStoreApplicationFacadeUnitTest {

    @InjectMocks
    SellerStoreApplicationFacade facade;

    @Mock
    S3Service s3Service;

    @Mock
    SellerService sellerService;

    @Mock
    SellerStoreService sellerStoreService;

    @Mock
    SellerStoreApplicationService sellerStoreApplicationService;

    @Mock
    SellerStoreApplicationMapper mapper;

    @Mock
    MultipartFile multipartFile;

    @Nested
    @DisplayName("registerStoreForSeller() 테스트")
    class RegisterStoreForSellerTest {

        @Test
        @DisplayName("유용한 스토어 정보가 주어지는 경우 스토어 등록 신청에 성공한다.")
        void success() {

            // given
            Long sellerId = 1L;
            // String imagePath = "seller-images/uuid-test.png";

            Seller seller = mock(Seller.class);
            // Store store = mock(Store.class);
            StoreApplication storeApplication = mock(StoreApplication.class);
            StoreApplicationDetail storeApplicationDetail = mock(StoreApplicationDetail.class);
            StoreApplicationCreateRequest request = mock(StoreApplicationCreateRequest.class);

            given(request.profile()).willReturn(DEFAULT_PROFILE);
            given(request.storeId()).willReturn(null);
            given(request.storeName()).willReturn(DEFAULT_STORE_NAME);

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(sellerStoreService.findStoreByStoreName(DEFAULT_STORE_NAME)).willReturn(Optional.empty());

            given(sellerStoreApplicationService.createStoreApplication(
                eq(request), isNull(), eq(seller), isNull()
            )).willReturn(storeApplication);

            given(mapper.toStoreApplicationDetail(storeApplication)).willReturn(storeApplicationDetail);

            // when
            StoreApplicationDetail result = facade.registerStoreForSeller(sellerId, request, multipartFile);

            // then
            assertThat(result).isEqualTo(storeApplicationDetail);
            verify(sellerService).updateSellerStatus(seller, CertificationStatus.PENDING);
            verify(s3Service, never()).deleteImage(any());
        }

        @Test
        @DisplayName("프로필 이미지와 프로필 경로 모두 없으면 INVALID_PROFILE 예외가 발생한다.")
        void fail_without_profile() {

            // given
            StoreApplicationCreateRequest request = mock(StoreApplicationCreateRequest.class);
            given(request.profile()).willReturn(null);

            // when & then
            assertThatThrownBy(() -> facade.registerStoreForSeller(
                1L, request,null))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_PROFILE);
                });

            verifyNoInteractions(sellerService);
        }

        @Test
        @DisplayName("이미 스토어를 등록했거나 승인 대기 중인 판매자는 ALREADY_REGISTER_STORE 예외가 발생한다.")
        void fail_with_alreadyRegisteredSeller() {

            // given
            Long sellerId = 1L;
            Seller seller = mock(Seller.class);

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            doThrow(new BbangleException(BbangleErrorCode.ALREADY_REGISTER_STORE)).when(seller).validateRegisterAvailable();

            // when & then
            assertThatThrownBy(() -> facade.registerStoreForSeller(
                sellerId, mock(StoreApplicationCreateRequest.class), multipartFile))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ALREADY_REGISTER_STORE);
                });

            verify(s3Service, never()).saveAndReturnWithCdn(any(), any());
        }

        @Test
        @DisplayName("Store Name이 중복일 경우 INVALID_STORE_NAME 예외가 발생한다.")
        void fail_duplicate_storeName() {

            // given
            Long sellerId = 1L;
            Seller seller = mock(Seller.class);
            StoreApplicationCreateRequest request = mock(StoreApplicationCreateRequest.class);

            given(request.profile()).willReturn(DEFAULT_PROFILE);
            given(request.storeId()).willReturn(null);
            given(request.storeName()).willReturn(DEFAULT_STORE_NAME);

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(sellerStoreService.findStoreByStoreName(DEFAULT_STORE_NAME))
                .willReturn(Optional.of(mock(Store.class)));

            // when & then
            assertThatThrownBy(() ->
                facade.registerStoreForSeller(sellerId, request,null))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_STORE_NAME);
                });
        }

        @Test
        @DisplayName("이미 등록된 스토어는 등록할 수 없다.")
        void fail_already_reserved_store() {

            // given
            Long sellerId = 1L;
            Seller seller = mock(Seller.class);
            StoreApplicationCreateRequest request = mock(StoreApplicationCreateRequest.class);

            given(request.profile()).willReturn(DEFAULT_PROFILE);
            given(request.storeId()).willReturn(10L);

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(sellerService.existsSellerByStoreId(10L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> facade.registerStoreForSeller(
                1L, request, multipartFile))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ALREADY_RESERVED_STORE);
                });
        }

        @Test
        @DisplayName("스토어 등록 신청 중 BbangleException 예외가 발생하면 S3 롤백 한다.")
        void fail_rollback_s3_by_bbangleException() {

            // given
            Long sellerId = 1L;
            Seller seller = mock(Seller.class);
            StoreApplicationCreateRequest request = mock(StoreApplicationCreateRequest.class);

            given(request.profile()).willReturn(DEFAULT_PROFILE);
            given(request.storeId()).willReturn(null);
            given(request.storeName()).willReturn(DEFAULT_STORE_NAME);

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(sellerStoreService.findStoreByStoreName(DEFAULT_STORE_NAME)).willReturn(Optional.empty());

            given(s3Service.saveAndReturnWithCdn(any(), any())).willReturn("cdn/error");

            given(sellerStoreApplicationService.createStoreApplication(
                any(), any(), any(), any()))
                .willThrow(new BbangleException(BbangleErrorCode._BAD_REQUEST));

            // when & then
            assertThatThrownBy(() -> facade.registerStoreForSeller(
                sellerId, request, multipartFile))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode._BAD_REQUEST);
                });

            verify(sellerService).updateSellerStatus(seller, CertificationStatus.NEW);
            verify(s3Service).deleteImage("cdn/error");
        }

        @Test
        @DisplayName("스토어 등록 신청 중 Exception 예외가 발생하면 S3 롤백 한다.")
        void fail_rollback_s3_by_exception() {

            // given
            Long sellerId = 1L;
            Seller seller = mock(Seller.class);
            StoreApplicationCreateRequest request = mock(StoreApplicationCreateRequest.class);

            given(request.profile()).willReturn(DEFAULT_PROFILE);
            given(request.storeId()).willReturn(null);
            given(request.storeName()).willReturn(DEFAULT_STORE_NAME);

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(sellerStoreService.findStoreByStoreName(DEFAULT_STORE_NAME)).willReturn(Optional.empty());

            given(s3Service.saveAndReturnWithCdn(anyString(), eq(multipartFile))).willReturn("cdn/error");

            given(sellerStoreApplicationService.createStoreApplication(
                any(), any(), any(), any()))
                .willThrow(new RuntimeException());

            // when & then
            assertThatThrownBy(() -> facade.registerStoreForSeller(
                sellerId, request, multipartFile))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.SELLER_CREATION_FAILED);
                });

            verify(sellerService).updateSellerStatus(seller, CertificationStatus.NEW);
            verify(s3Service).deleteImage("cdn/error");
        }
    }

    @Nested
    @DisplayName("findStoreApplication() 테스트")
    class FindStoreApplicationTest {

        @Test
        @DisplayName("Store 등록 신청 정보가 존재할 경우 상세 정보를 반환한다.")
        void exist() {

            // given
            Long sellerId = 1L;
            StoreApplication application = mock(StoreApplication.class);
            StoreApplicationDetail detail = mock(StoreApplicationDetail.class);

            given(sellerStoreApplicationService.findStoreApplicationBySellerId(sellerId))
                .willReturn(Optional.of(application));
            given(mapper.toStoreApplicationDetail(application)).willReturn(detail);

            // when
            StoreApplicationDetail result = facade.findStoreApplication(sellerId);

            // then
            assertThat(result).isEqualTo(detail);
            verify(mapper).toStoreApplicationDetail(application);
        }

        @Test
        @DisplayName("Store 등록 신청 정보가 없을 경우 null을 반환한다.")
        void notExist() {

            // given
            Long sellerId = 1L;

            given(sellerStoreApplicationService.findStoreApplicationBySellerId(sellerId))
                .willReturn(Optional.empty());

            // when
            StoreApplicationDetail result = facade.findStoreApplication(sellerId);

            // then
            assertThat(result).isNull();
            verify(mapper, never()).toStoreApplicationDetail(any());
        }
    }
}