package com.bbangle.bbangle.store.seller.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.bbangle.bbangle.config.S3IntegrationTestSupport;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.StoreRegisterResult;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@DisplayName("[통합테스트] SellerStoreFacade")
@Transactional
class SellerStoreFacadeIntegrationTest extends S3IntegrationTestSupport {

    @Autowired
    private SellerStoreFacade sellerStoreFacade;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

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
        @DisplayName("유용한 정보가 주어지면 스토어 등록에 성공한다.")
        class Success_registerStoreForSeller {

            private Seller saveNewSeller() {
                Seller seller = SellerFixture.defaultSellerMergeConflict();
                return sellerRepository.saveAndFlush(seller);
            }

            @Test
            @DisplayName("storeId가 없는 경우")
            void without_storeId() {

                // given
                Seller seller = saveNewSeller();
                StoreRequest.StoreCreateRequest request =
                    new StoreRequest.StoreCreateRequest(
                        "빵그리의 오븐",
                        null,
                        "비건 베이커리",
                        "01012345678",
                        "01098765432",
                        "test@gmail.com",
                        "경기도 수원시",
                        "상세주소",
                        null
                    );

                MockMultipartFile mockFile = mockProfileImage();

                em.flush();
                em.clear();

                // when
                StoreRegisterResult result = sellerStoreFacade.registerStoreForSeller(seller.getId(), request, mockFile);
                em.flush();
                em.clear();

                // then
                Seller updatedSeller = sellerRepository.findById(seller.getId()).orElseThrow();
                Store store = storeRepository.findAll().get(0);

                assertThat(updatedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
                assertThat(store.getStatus()).isEqualTo(StoreStatus.RESERVED);
                assertThat(result.sellerId()).isEqualTo(seller.getId());
            }

            @Test
            @DisplayName("storeId가 존재하는 경우")
            void with_storeId() {

                // given
                Seller seller = saveNewSeller();
                Store store = StoreFixture.defaultStore();
                Store newStore = storeRepository.save(store);

                StoreRequest.StoreCreateRequest request =
                    new StoreRequest.StoreCreateRequest(
                        "빵그리의 오븐",
                        newStore.getProfile(),
                        "비건 베이커리",
                        "01012345678",
                        "01098765432",
                        "test@gmail.com",
                        "경기도 수원시",
                        "상세주소",
                        newStore.getId()
                    );

                MockMultipartFile mockFile = mockProfileImage();

                em.flush();
                em.clear();

                // when
                StoreRegisterResult result = sellerStoreFacade.registerStoreForSeller(seller.getId(), request, mockFile);
                em.flush();
                em.clear();

                // then
                Seller updatedSeller = sellerRepository.findById(seller.getId()).orElseThrow();
                Store savedStore = storeRepository.findById(result.store().storeId()).orElseThrow();

                assertThat(updatedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
                assertThat(savedStore.getStatus()).isEqualTo(StoreStatus.RESERVED);
                assertThat(result.sellerId()).isEqualTo(seller.getId());
            }
        }

        @Nested
        @DisplayName("스토어 등록에 실패한다.")
        class Fail_registerStoreForSeller {

            private Seller saveNewSeller(CertificationStatus status) {
                Seller seller = SellerFixture.defaultSellerMergeConflict(status);
                return sellerRepository.saveAndFlush(seller);
            }

            @Test
            @DisplayName("이미 등록된 판매자 계정인 경우")
            void already_registered_seller() {

                // given
                Seller seller = saveNewSeller(CertificationStatus.PENDING);
                StoreRequest.StoreCreateRequest request = mock(StoreRequest.StoreCreateRequest.class);

                // when & then
                assertThatThrownBy(() ->
                    sellerStoreFacade.registerStoreForSeller(seller.getId(), request, mock(MultipartFile.class))
                )
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
                Seller seller = saveNewSeller(CertificationStatus.NEW);

                Store store = StoreFixture.defaultStore(StoreStatus.RESERVED);
                Store newStore = storeRepository.saveAndFlush(store);

                StoreRequest.StoreCreateRequest request =
                    new StoreRequest.StoreCreateRequest(
                        "빵그리의 오븐",
                        newStore.getProfile(),
                        "비건 베이커리",
                        "01012345678",
                        "01098765432",
                        "test@gmail.com",
                        "경기도 수원시",
                        "상세주소",
                        newStore.getId()
                    );

                MockMultipartFile mockFile = mockProfileImage();

                // when & then
                assertThatThrownBy(() ->
                    sellerStoreFacade.registerStoreForSeller(seller.getId(), request, mockFile)
                )
                    .isInstanceOf(BbangleException.class)
                    .satisfies(e -> {
                        BbangleException ex = (BbangleException) e;
                        assertThat(ex.getBbangleErrorCode())
                            .isEqualTo(BbangleErrorCode.ALREADY_RESERVED_STORE);
                    });
            }
        }
    }
}