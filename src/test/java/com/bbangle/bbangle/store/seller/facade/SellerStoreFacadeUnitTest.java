package com.bbangle.bbangle.store.seller.facade;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.StoreRegisterResult;
import com.bbangle.bbangle.store.seller.controller.mapper.SellerStoreMapper;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
    S3Service s3Service;

    @Mock
    SellerStoreService sellerStoreService;

    @Mock
    SellerService sellerService;

    @Mock
    SellerStoreMapper sellerStoreMapper;

    @Mock
    MultipartFile multipartFile;

    @Nested
    @DisplayName("registerStoreForSeller() 테스트")
    class RegisterStoreForSellerTest {

        @Test
        @DisplayName("유용한 스토어 정보가 주어지는 경우 스토어 등록에 성공한다.")
        void success_registerStoreForSeller_with_valid_storeDetail() {

            // given
            Long sellerId = 1L;
            String imagePath = "seller-images/uuid-test.png";

            Seller seller = mock(Seller.class);
            Store store = mock(Store.class);
            StoreRequest.StoreCreateRequest request = mock(StoreRequest.StoreCreateRequest.class);
            StoreResponse.SellerStoreDetail storeDetail = mock(StoreResponse.SellerStoreDetail.class);

            given(sellerService.getSellerById(sellerId)).willReturn(seller);
            given(seller.getCertificationStatus()).willReturn(CertificationStatus.NEW);
            given(s3Service.saveAndReturnWithCdn(anyString(), eq(multipartFile))).willReturn(imagePath);
            given(sellerStoreService.createStore(request, imagePath)).willReturn(store);
            given(store.getStatus()).willReturn(StoreStatus.NONE);
            given(sellerStoreMapper.toSellerStoreDetail(store)).willReturn(storeDetail);

            // when
            StoreRegisterResult result = sellerStoreFacade.registerStoreForSeller(sellerId, request, multipartFile);

            // then
            assertThat(result).isNotNull();
            verify(sellerStoreService).registerStore(seller, store);
            verify(s3Service, never()).deleteImage(imagePath);
        }

        @ParameterizedTest
        @EnumSource(value = CertificationStatus.class, names = {"PENDING", "APPROVED"})
        @DisplayName("이미 스토어를 등록한 판매자는 예외가 발생한다.")
        void fail_registerStoreForSeller_when_certificationStatus_isNot_new(CertificationStatus status) {

            // given
            Seller seller = mock(Seller.class);

            given(sellerService.getSellerById(1L)).willReturn(seller);
            given(seller.getCertificationStatus()).willReturn(status);

            // when & then
            Assertions.assertThatThrownBy(() -> sellerStoreFacade.registerStoreForSeller(
                    1L, mock(StoreRequest.StoreCreateRequest.class), multipartFile
                ))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    Assertions.assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ALREADY_REGISTER_STORE);
                });

            verify(s3Service, never()).saveAndReturnWithCdn(any(), any());
        }

        @ParameterizedTest
        @EnumSource(value = StoreStatus.class, names = {"RESERVED", "ACTIVE"})
        @DisplayName("이미 등록된 스토어는 등록할 수 없다.")
        void fail_registerStoreForSeller_already_reserved_store(StoreStatus status) {

            // given
            Seller seller = mock(Seller.class);
            Store store = mock(Store.class);

            given(sellerService.getSellerById(1L)).willReturn(seller);
            given(seller.getCertificationStatus()).willReturn(CertificationStatus.NEW);
            given(s3Service.saveAndReturnWithCdn(any(), any())).willReturn("cdn/path");
            given(sellerStoreService.createStore(any(), any())).willReturn(store);
            given(store.getStatus()).willReturn(status);

            // when & then
            Assertions.assertThatThrownBy(() -> sellerStoreFacade.registerStoreForSeller(
                    1L, mock(StoreRequest.StoreCreateRequest.class), multipartFile
                ))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    Assertions.assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ALREADY_RESERVED_STORE);
                });

            verify(s3Service).deleteImage(any());
        }

        @Test
        @DisplayName("판매자 생성 중 DB 예외가 발생하면 업로드된 S3 이미지를 삭제하고 예외를 던진다.")
        void fail_registerStoreForSeller_rollback_s3_by_exception() {

            // given
            Seller seller = mock(Seller.class);

            given(sellerService.getSellerById(1L)).willReturn(seller);
            given(seller.getCertificationStatus()).willReturn(CertificationStatus.NEW);
            given(s3Service.saveAndReturnWithCdn(any(), any())).willReturn("cdn/error");
            given(sellerStoreService.createStore(any(), any())).willThrow(new RuntimeException());

            // when & then
            Assertions.assertThatThrownBy(() -> sellerStoreFacade.registerStoreForSeller(
                    1L, mock(StoreRequest.StoreCreateRequest.class), multipartFile
                ))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    Assertions.assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.SELLER_CREATION_FAILED);
                });

            verify(s3Service).deleteImage("cdn/error");
        }

        @Test
        @DisplayName("프로필 이미지와 프로필 경로 모두 없으면 예외가 발생한다.")
        void fail_registerStoreForSeller_without_profile() {

            // given
            StoreRequest.StoreCreateRequest request = mock(StoreRequest.StoreCreateRequest.class);
            given(request.profile()).willReturn(null);

            // when & then
            assertThatThrownBy(() ->
                sellerStoreFacade.registerStoreForSeller(1L, request,null))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_PROFILE);
                });
        }
    }
}