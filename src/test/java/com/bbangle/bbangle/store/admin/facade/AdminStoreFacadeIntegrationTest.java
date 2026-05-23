package com.bbangle.bbangle.store.admin.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.config.S3IntegrationTestSupport;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.store.admin.controller.dto.StoreDetailRequestFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest.StoreDetailRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.StoreDetailResponse;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@DisplayName("[통합테스트] AdminStoreFacade")
@Transactional
class AdminStoreFacadeIntegrationTest extends S3IntegrationTestSupport {

    @Autowired
    private AdminStoreFacade adminStoreFacade;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("createStoreForAdmin() 테스트")
    class CreateStoreForAdminTest {

        private MockMultipartFile mockProfileImage() {
            return new MockMultipartFile(
                "profile",
                "profile.png",
                "image/png",
                "test".getBytes()
            );
        }

        @Test
        @DisplayName("유용한 스토어 정보가 주어지는 경우 스토어 생성에 성공한다.")
        void success_createStoreForAdmin() {

            // given
            StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture();
            MultipartFile profileImage = mockProfileImage();

            // when
            StoreDetailResponse result = adminStoreFacade.createStoreForAdmin(request, profileImage);

            em.flush();
            em.clear();

            // then
            Store savedStore = storeRepository.findById(result.storeId()).orElseThrow();

            assertThat(savedStore.getName()).isEqualTo(request.storeName());
            assertThat(savedStore.getIdentifier()).isEqualTo(request.identifier());
            assertThat(savedStore.getProfile()).isEqualTo(result.profile());
            assertThat(savedStore.getIntroduce()).isEqualTo(request.introduce());
        }

        @Test
        @DisplayName("스토어 이름이 중복되면 예외 발생")
        void fail_createStoreForAdmin_duplicateStoreName() {

            // given
            Store store = storeRepository.save(StoreFixture.defaultStore());
            StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture(store.getName());
            MultipartFile profileImage = mockProfileImage();

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> adminStoreFacade.createStoreForAdmin(request, profileImage)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_STORE_NAME);
                });
        }

        @Test
        @DisplayName("프로필 이미지가 없으면 예외 발생")
        void fail_createStoreForAdmin_invalidProfile() {

            // given
            StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture();

            // when & then
            assertThatThrownBy(() -> adminStoreFacade.createStoreForAdmin(request, null)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_PROFILE);
                });
        }
    }
}