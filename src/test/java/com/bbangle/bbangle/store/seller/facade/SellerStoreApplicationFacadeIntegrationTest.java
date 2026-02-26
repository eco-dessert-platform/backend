package com.bbangle.bbangle.store.seller.facade;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.bbangle.bbangle.config.S3IntegrationTestSupport;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreApplicationFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.fixture.store.seller.controller.dto.StoreApplicationRequestFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.domain.model.StoreApplicationStatus;
import com.bbangle.bbangle.store.repository.StoreApplicationRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationRequest.StoreApplicationCreateRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationResponse.StoreApplicationDetail;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] SellerStoreApplicationFacade")
@Transactional
class SellerStoreApplicationFacadeIntegrationTest extends S3IntegrationTestSupport {

    @Autowired
    private SellerStoreApplicationFacade facade;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreApplicationRepository storeApplicationRepository;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("registerStoreForSeller() 테스트")
    class RegisterStoreForSellerTest {

        private MockMultipartFile mockProfileImage() {
            return new MockMultipartFile(
                "profileImage",             // 파라미터 이름 (컨트롤러에서 받는 이름)
                "test-image.png",           // 파일명
                "image/png",                // Content-Type
                "fake image content".getBytes()  // 파일 내용
            );
        }

        @Nested
        @DisplayName("유용한 정보가 주어지면 스토어 등록 신청에 성공한다.")
        class Success_registerStoreForSeller {

            private Seller saveNewSeller() {
                return sellerRepository.saveAndFlush(SellerFixture.defaultSeller());
            }

            @Test
            @DisplayName("storeId가 없는 경우")
            void without_storeId() {

                // given
                Seller seller = saveNewSeller();
                StoreApplicationCreateRequest request =
                    StoreApplicationRequestFixture.defaultStoreApplicationCreateRequest(null, null);

                MockMultipartFile mockFile = mockProfileImage();

                // when
                StoreApplicationDetail result = facade.registerStoreForSeller(seller.getId(), request, mockFile);
                em.flush();
                em.clear();

                // then
                Seller updatedSeller = sellerRepository.findById(seller.getId()).orElseThrow();

                assertThat(result.status()).isEqualTo(StoreApplicationStatus.PENDING);
                assertThat(updatedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
                assertThat(result.sellerId()).isEqualTo(seller.getId());
                assertThat(result.storeId()).isNull();
            }

            @Test
            @DisplayName("storeId가 존재하는 경우")
            void with_storeId() {

                // given
                Seller seller = saveNewSeller();
                Store store = storeRepository.saveAndFlush(StoreFixture.defaultStore());
                StoreApplicationCreateRequest request =
                    StoreApplicationRequestFixture.defaultStoreApplicationCreateRequest(null, store.getId());

                MockMultipartFile mockFile = mockProfileImage();

                // when
                StoreApplicationDetail result = facade.registerStoreForSeller(seller.getId(), request, mockFile);
                em.flush();
                em.clear();

                // then
                Seller updatedSeller = sellerRepository.findById(seller.getId()).orElseThrow();
                Store savedStore = storeRepository.findById(result.storeId()).orElseThrow();

                assertThat(result.status()).isEqualTo(StoreApplicationStatus.PENDING);
                assertThat(updatedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
                assertThat(result.sellerId()).isEqualTo(updatedSeller.getId());
                assertThat(result.storeId()).isEqualTo(savedStore.getId());
            }
        }

        @Nested
        @DisplayName("스토어 등록 신청에 실패한다.")
        class Fail_registerStoreForSeller {

            private Seller saveNewSeller(CertificationStatus status) {
                return sellerRepository.saveAndFlush(SellerFixture.defaultSeller(status));
            }

            @Test
            @DisplayName("프로필 이미지와 프로필 경로 모두 없는 경우")
            void without_profile() {

                // given
                Seller seller = saveNewSeller(CertificationStatus.NEW);
                StoreApplicationCreateRequest request =
                    StoreApplicationRequestFixture.defaultStoreApplicationCreateRequest(null, null);

                // when & then
                assertThatThrownBy(() -> facade.registerStoreForSeller(
                    seller.getId(), request, null))
                    .isInstanceOf(BbangleException.class)
                    .satisfies(e -> {
                        BbangleException ex = (BbangleException) e;
                        assertThat(ex.getBbangleErrorCode())
                            .isEqualTo(BbangleErrorCode.INVALID_PROFILE);
                    });
            }

            @Test
            @DisplayName("Store Name이 중복인 경우")
            void duplicate_storeName() {

                // given
                Seller seller = saveNewSeller(CertificationStatus.NEW);
                storeRepository.saveAndFlush(StoreFixture.defaultStore());
                StoreApplicationCreateRequest request =
                    StoreApplicationRequestFixture.defaultStoreApplicationCreateRequest(DEFAULT_PROFILE, null);

                // when & then
                assertThatThrownBy(() -> facade.registerStoreForSeller(
                    seller.getId(), request, null))
                    .isInstanceOf(BbangleException.class)
                    .satisfies(e -> {
                        BbangleException ex = (BbangleException) e;
                        assertThat(ex.getBbangleErrorCode())
                            .isEqualTo(BbangleErrorCode.INVALID_STORE_NAME);
                    });
            }

            @Test
            @DisplayName("이미 등록된 판매자 계정인 경우")
            void already_registered_seller() {

                // given
                Seller seller = saveNewSeller(CertificationStatus.PENDING);
                StoreApplicationCreateRequest request =
                    StoreApplicationRequestFixture.defaultStoreApplicationCreateRequest(DEFAULT_PROFILE, null);

                // when & then
                assertThatThrownBy(() -> facade.registerStoreForSeller(
                    seller.getId(), request, null))
                    .isInstanceOf(BbangleException.class)
                    .satisfies(e -> {
                        BbangleException ex = (BbangleException) e;
                        assertThat(ex.getBbangleErrorCode())
                            .isEqualTo(BbangleErrorCode.ALREADY_REGISTER_STORE);
                    });
            }

            @Test
            @DisplayName("이미 등록된 스토어인 경우")
            void already_registered_store() {

                // given
                Store store = storeRepository.saveAndFlush(StoreFixture.defaultStore());
                sellerRepository.saveAndFlush(SellerFixture.defaultSeller(store));

                Seller seller = saveNewSeller(CertificationStatus.NEW);

                StoreApplicationCreateRequest request =
                    StoreApplicationRequestFixture.defaultStoreApplicationCreateRequest(DEFAULT_PROFILE, store.getId());

                // when & then
                assertThatThrownBy(() -> facade.registerStoreForSeller(
                    seller.getId(), request, null))
                    .isInstanceOf(BbangleException.class)
                    .satisfies(e -> {
                        BbangleException ex = (BbangleException) e;
                        assertThat(ex.getBbangleErrorCode())
                            .isEqualTo(BbangleErrorCode.ALREADY_RESERVED_STORE);
                    });
            }
        }
    }

    @Nested
    @DisplayName("findStoreApplication() 테스트")
    class FindStoreApplicationTest {

        @Test
        @DisplayName("Store 등록 신청 정보가 존재할 경우 상세 정보를 반환한다.")
        void exist() {

            // given
            Seller seller = sellerRepository.saveAndFlush(SellerFixture.defaultSeller());
            StoreApplication application = storeApplicationRepository.saveAndFlush(
                StoreApplicationFixture.defaultStoreApplication(seller, null)
            );

            // when
            StoreApplicationDetail result = facade.findStoreApplication(seller.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.sellerId()).isEqualTo(seller.getId());
            assertThat(result.storeApplicationId()).isEqualTo(application.getId());
            assertThat(result.status()).isEqualTo(StoreApplicationStatus.PENDING);
        }

        @Test
        @DisplayName("Store 등록 신청 정보가 없을 경우 null을 반환한다.")
        void notExist() {

            // given
            Seller seller = sellerRepository.saveAndFlush(SellerFixture.defaultSeller());
            storeApplicationRepository.saveAndFlush(StoreApplicationFixture.defaultStoreApplication(seller, null));

            // when
            StoreApplicationDetail result = facade.findStoreApplication(seller.getId());

            // then
            assertThat(result).isNull();
        }
    }
}