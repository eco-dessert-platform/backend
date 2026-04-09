package com.bbangle.bbangle.store.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[슬라이스 테스트] StoreNameRequestRepository")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class StoreNameRequestRepositoryTest {

    @Autowired
    private StoreNameRequestRepository repository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("findActiveRequestsBySellerId() 테스트")
    class FindActiveRequestsBySellerIdTest {

        private Seller testSeller;
        private Store testStore;

        @BeforeEach
        void setUp() {
            testStore = storeRepository.save(StoreFixture.defaultStore());
            testSeller = sellerRepository.save(SellerFixture.defaultSeller(testStore));

            em.flush();
            em.clear();
        }

        private void saveRequest(Seller seller, Store store, StoreApprovalStatus status) {
            StoreNameRequest request = StoreNameRequestFixture.defaultStoreNameRequest(seller, store, status);
            repository.save(request);
        }

        @Test
        @DisplayName("APPROVE 상태인 신청 데이터가 존재하면 APPROVE를 우선적으로 반환한다.")
        void returnApproveIfExists() {

            // given
            saveRequest(testSeller, testStore, StoreApprovalStatus.PENDING);
            saveRequest(testSeller, testStore, StoreApprovalStatus.PENDING);
            saveRequest(testSeller, testStore, StoreApprovalStatus.REJECT);
            saveRequest(testSeller, testStore, StoreApprovalStatus.APPROVE);

            em.flush();
            em.clear();

            // when
            Optional<StoreApprovalStatus> result = repository.findActiveRequestsBySellerId(testSeller.getId());

            // then
            assertThat(result).contains(StoreApprovalStatus.APPROVE);
        }

        @Test
        @DisplayName("APPROVE 상태인 신청 데이터가 없고 PENDING만 있으면 PENDING을 반환한다.")
        void returnPendingIfOnlyPending() {

            // given
            saveRequest(testSeller, testStore, StoreApprovalStatus.PENDING);

            em.flush();
            em.clear();

            // when
            Optional<StoreApprovalStatus> result = repository.findActiveRequestsBySellerId(testSeller.getId());

            // then
            assertThat(result).contains(StoreApprovalStatus.PENDING);
        }

        @Test
        @DisplayName("APPROVE나 PENDING 상태인 데이터가 없으면 empty을 반환한다.")
        void returnEmptyIfNoActiveRequest() {

            // given
            saveRequest(testSeller, testStore, StoreApprovalStatus.REJECT);

            em.flush();
            em.clear();

            // when
            Optional<StoreApprovalStatus> result = repository.findActiveRequestsBySellerId(testSeller.getId());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("다른 판매자가 신청한 데이터는 영향이 없다.")
        void ignoreOtherSellerData() {

            // given
            Store otherStore = storeRepository.save(StoreFixture.defaultStore());
            Seller otherSeller = SellerFixture.defaultSeller("otherSeller", otherStore);
            sellerRepository.save(otherSeller);

            saveRequest(testSeller, testStore, StoreApprovalStatus.PENDING);
            saveRequest(otherSeller, otherStore, StoreApprovalStatus.PENDING);

            em.flush();
            em.clear();

            // when
            Optional<StoreApprovalStatus> result = repository.findActiveRequestsBySellerId(testSeller.getId());

            // then
            assertThat(result).contains(StoreApprovalStatus.PENDING);
        }
    }
}