package com.bbangle.bbangle.store.admin.facade;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.store.admin.controller.dto.StoreDetailRequestFixture;
import com.bbangle.bbangle.fixture.store.admin.service.model.AdminStoreInfoFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest.StoreDetailRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.StoreDetailResponse;
import com.bbangle.bbangle.store.admin.controller.mapper.AdminStoreMapper;
import com.bbangle.bbangle.store.admin.service.AdminStoreService;
import com.bbangle.bbangle.store.admin.service.model.AdminStoreInfo;
import com.bbangle.bbangle.store.domain.Store;
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

@DisplayName("[단위테스트] AdminStoreFacade")
@ExtendWith(MockitoExtension.class)
class AdminStoreFacadeUnitTest {

    @InjectMocks
    AdminStoreFacade adminStoreFacade;

    @Mock
    S3Service s3Service;

    @Mock
    AdminStoreService adminStoreService;

    @Mock
    SellerStoreService sellerStoreService;

    @Mock
    AdminStoreMapper adminStoreMapper;

    @Mock
    MultipartFile multipartFile;

    @Nested
    @DisplayName("createStoreForAdmin() 테스트")
    class CreateStoreForAdminTest {

        @Test
        @DisplayName("유용한 스토어 정보가 주어지는 경우 스토어 생성에 성공한다.")
        void success_createStoreForAdmin() {

            // given
            StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture();
            AdminStoreInfo adminStoreInfo = AdminStoreInfoFixture.defaultAdminStoreInfo();
            Store store = StoreFixture.defaultStore();
            StoreDetailResponse response = StoreDetailResponse.builder()
                .storeId(1L)
                .name(store.getName())
                .identifier(store.getIdentifier())
                .introduce(store.getIntroduce())
                .profile(store.getProfile())
                .phoneNumber(store.getPhoneNumberVO().getPhoneNumber())
                .subPhoneNumber(store.getPhoneNumberVO().getSubPhoneNumber())
                .email(store.getEmailVO().getEmail())
                .originAddress(store.getOriginAddressLine())
                .originAddressDetail(store.getOriginAddressDetail())
                .build();

            given(sellerStoreService.findStoreByStoreName(request.storeName())).willReturn(Optional.empty());
            given(s3Service.saveAndReturnWithCdn(anyString(), eq(multipartFile))).willReturn(DEFAULT_PROFILE);
            given(adminStoreMapper.toAdminStoreInfo(request, DEFAULT_PROFILE)).willReturn(adminStoreInfo);
            given(adminStoreService.createStore(adminStoreInfo)).willReturn(store);
            given(adminStoreMapper.toStoreDetailResponse(store)).willReturn(response);

            // when
            StoreDetailResponse result = adminStoreFacade.createStoreForAdmin(request, multipartFile);

            // then
            assertThat(result).isEqualTo(response);

            verify(s3Service).saveAndReturnWithCdn(anyString(), eq(multipartFile));
            verify(adminStoreService).createStore(adminStoreInfo);
            verify(s3Service, never()).deleteImage(anyString());
        }

        @Test
        @DisplayName("스토어 이름이 중복되면 예외 발생")
        void fail_createStoreForAdmin_duplicateStoreName() {

            // given
            StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture();

            given(sellerStoreService.findStoreByStoreName(request.storeName()))
                .willReturn(Optional.of(StoreFixture.defaultStore()));

            // when & then
            assertThatThrownBy(() -> adminStoreFacade.createStoreForAdmin(request, multipartFile)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_STORE_NAME);
                });

            verify(s3Service, never()).saveAndReturnWithCdn(anyString(), any());
        }

        @Test
        @DisplayName("프로필 이미지가 없으면 예외 발생")
        void fail_createStoreForAdmin_invalidProfile() {

            // given
            StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture();

            given(sellerStoreService.findStoreByStoreName(request.storeName())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminStoreFacade.createStoreForAdmin(request, null)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_PROFILE);
                });

            verify(s3Service, never()).saveAndReturnWithCdn(anyString(), any());
        }

        @Test
        @DisplayName("스토어 생성 실패 시 업로드된 이미지를 롤백한다")
        void fail_createStoreForAdmin_rollbackS3() {

            // given
            StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture();
            AdminStoreInfo adminStoreInfo = AdminStoreInfoFixture.defaultAdminStoreInfo();

            given(sellerStoreService.findStoreByStoreName(request.storeName())).willReturn(Optional.empty());
            given(s3Service.saveAndReturnWithCdn(anyString(), eq(multipartFile))).willReturn(DEFAULT_PROFILE);
            given(adminStoreMapper.toAdminStoreInfo(request, DEFAULT_PROFILE)).willReturn(adminStoreInfo);
            given(adminStoreService.createStore(adminStoreInfo)).willThrow(new BbangleException(BbangleErrorCode.ALREADY_RESERVED_STORE));

            // when & then
            assertThatThrownBy(() -> adminStoreFacade.createStoreForAdmin(request, multipartFile))
                .isInstanceOf(BbangleException.class);

            verify(s3Service).deleteImage(DEFAULT_PROFILE);
        }

        @Test
        @DisplayName("예상치 못한 예외 발생 시 STORE_CREATION_FAILED 예외 발생")
        void fail_createStoreForAdmin_internalException() {

            // given
            StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture();
            AdminStoreInfo adminStoreInfo = AdminStoreInfoFixture.defaultAdminStoreInfo();

            given(sellerStoreService.findStoreByStoreName(request.storeName())).willReturn(Optional.empty());
            given(s3Service.saveAndReturnWithCdn(anyString(), eq(multipartFile))).willReturn(DEFAULT_PROFILE);
            given(adminStoreMapper.toAdminStoreInfo(request, DEFAULT_PROFILE)).willReturn(adminStoreInfo);
            given(adminStoreService.createStore(adminStoreInfo)).willThrow(new RuntimeException("DB Error"));

            // when & then
            assertThatThrownBy(() -> adminStoreFacade.createStoreForAdmin(request, multipartFile)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.STORE_CREATION_FAILED);
                });

            verify(s3Service).deleteImage(DEFAULT_PROFILE);
        }

        @Test
        @DisplayName("S3 업로드 실패 시 예외 발생")
        void fail_createStoreForAdmin_s3UploadFail() {

            // given
            StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture();

            given(sellerStoreService.findStoreByStoreName(request.storeName())).willReturn(Optional.empty());
            given(s3Service.saveAndReturnWithCdn(anyString(), any())).willThrow(new RuntimeException("S3 Error"));

            // when & then
            assertThatThrownBy(() -> adminStoreFacade.createStoreForAdmin(request, multipartFile)
            )
                .isInstanceOf(RuntimeException.class)
                .hasMessage("S3 Error");

            verify(s3Service, never()).deleteImage(anyString());
        }
    }
}