package com.bbangle.bbangle.store.seller.service;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PROFILE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreApplicationFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.fixture.store.seller.controller.dto.StoreApplicationRequestFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.repository.StoreApplicationRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationRequest;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] SellerStoreApplicationService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SellerStoreApplicationServiceIntegrationTest {

    @Autowired
    private SellerStoreApplicationService service;

    @Autowired
    private StoreApplicationRepository storeApplicationRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("createStoreApplication() 테스트")
    class CreateStoreApplicationTest {

        @Test
        @DisplayName("profileImagePath가 null이면 request의 profile을 저장한다.")
        void with_requestProfile() {

            // given
            StoreApplicationRequest.StoreApplicationCreateRequest request =
                StoreApplicationRequestFixture.defaultStoreApplicationCreateRequest(DEFAULT_PROFILE, null);
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller());

            // when
            StoreApplication result = service.createStoreApplication(request, null, seller, null);
            em.flush();
            em.clear();

            StoreApplication saved = storeApplicationRepository.findById(result.getId()).orElseThrow();

            // then
            assertThat(saved.getProfile()).isEqualTo(DEFAULT_PROFILE);
            assertThat(saved.getSeller().getId()).isEqualTo(seller.getId());
            assertThat(saved.getStore()).isNull();
            assertThat(saved.getName()).isEqualTo(DEFAULT_STORE_NAME);
        }

        @Test
        @DisplayName("profileImagePath가 존재하면 profileImagePath를 저장한다.")
        void with_profileImagePath() {

            // given
            StoreApplicationRequest.StoreApplicationCreateRequest request =
                StoreApplicationRequestFixture.defaultStoreApplicationCreateRequest(DEFAULT_PROFILE, null);
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller());
            String profileImagePath = "s3/test.jpg";

            // when
            StoreApplication result = service.createStoreApplication(request, profileImagePath, seller, null);
            em.flush();
            em.clear();

            StoreApplication saved = storeApplicationRepository.findById(result.getId()).orElseThrow();

            // then
            assertThat(saved.getProfile()).isEqualTo(profileImagePath);
            assertThat(saved.getSeller().getId()).isEqualTo(seller.getId());
            assertThat(saved.getStore()).isNull();
            assertThat(saved.getName()).isEqualTo(DEFAULT_STORE_NAME);
        }

        @Test
        @DisplayName("기존에 존재하던 Store에 대한 등록 신청일 경우 Store를 연결한다.")
        void with_existStore() {

            // given
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller());
            Store store = storeRepository.save(StoreFixture.defaultStore());
            StoreApplicationRequest.StoreApplicationCreateRequest request =
                StoreApplicationRequestFixture.defaultStoreApplicationCreateRequest(DEFAULT_PROFILE, store.getId());

            // when
            StoreApplication result = service.createStoreApplication(request, null, seller, store);
            em.flush();
            em.clear();

            StoreApplication saved = storeApplicationRepository.findById(result.getId()).orElseThrow();

            // then
            assertThat(saved.getStore()).isNotNull();
            assertThat(saved.getStore().getId()).isEqualTo(store.getId());
        }
    }

    @Nested
    @DisplayName("findStoreApplicationBySellerId() 테스트")
    class FindStoreApplicationBySellerIdTest {

        @Test
        @DisplayName("신청이 여러개면 가장 최신 스토어 등록 신청이 조회된다.")
        void latest_storeApplication() {

            // given
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller());

            storeApplicationRepository.saveAndFlush(StoreApplicationFixture.defaultStoreApplication(seller, null));

            StoreApplication storeApplication2 = storeApplicationRepository.saveAndFlush(
                StoreApplicationFixture.defaultStoreApplication(seller, null)
            );

            // when
            Optional<StoreApplication> result = service.findStoreApplicationBySellerId(seller.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(storeApplication2.getId());
        }

        @Test
        @DisplayName("스토어 등록 신청이 없으면 empty를 반환한다.")
        void empty_notExistStoreApplication() {

            // given
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller());

            // when
            Optional<StoreApplication> result = service.findStoreApplicationBySellerId(seller.getId());

            // then
            assertThat(result).isEmpty();
        }
    }
}