package com.bbangle.bbangle.store.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.seller.domain.AccountVerificationFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreApplicationFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList.SellerApplicationInfo;
import com.bbangle.bbangle.seller.domain.AccountVerification;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[슬라이스 테스트] StoreApplicationRepository")
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
class StoreApplicationRepositoryTest {

    @Autowired
    private StoreApplicationRepository repository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("판매자가 신청한 스토어 등록 정보 중 제일 최근 1개만 조회한다.")
    void success_findLatestBySellerId() {

        // given
        Seller seller = sellerRepository.saveAndFlush(SellerFixture.defaultSeller());
        Store store = storeRepository.saveAndFlush(StoreFixture.defaultStore());

        StoreApplication storeApplication1 = repository
            .saveAndFlush(StoreApplicationFixture.defaultStoreApplication(seller, store));
        StoreApplication storeApplication2 = repository
            .saveAndFlush(StoreApplicationFixture.defaultStoreApplication(seller, store));

        em.createQuery("""
                          update StoreApplication s
                          set s.createdAt = :time
                          where s.id = :id
                          """)
            .setParameter("time", LocalDateTime.of(2026,1,1,0,0))
            .setParameter("id", storeApplication1.getId())
            .executeUpdate();

        em.createQuery("""
                          update StoreApplication s
                          set s.createdAt = :time
                          where s.id = :id
                          """)
            .setParameter("time", LocalDateTime.of(2026,2,1,0,0))
            .setParameter("id", storeApplication2.getId())
            .executeUpdate();

        em.clear();

        // when
        Optional<StoreApplication> result = repository.findLatestBySellerId(seller.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(storeApplication2.getId());
    }

    @Nested
    @DisplayName("findSellerApplications() 테스트")
    class FindSellerApplicationsTest {

        private Seller createSeller() {
            Seller seller = SellerFixture.defaultSeller();
            em.persist(seller);
            return seller;
        }

        private void createAccountVerification(Seller seller, String accountNumber, boolean verified) {
            AccountVerification av = AccountVerificationFixture.defaultAccountVerification(seller, accountNumber, verified);
            em.persist(av);
        }

        private void createStoreApplication(Seller seller, String name, StoreApprovalStatus status) {
            StoreApplication sa = StoreApplicationFixture.defaultStoreApplication(name, seller, status);
            em.persist(sa);
        }

        @Test
        @DisplayName("판매자의 스토어 등록 신청 목록을 조회한다.")
        void success_findSellerApplications() {

            // given
            Seller seller = createSeller();

            createAccountVerification(seller, "333", true);

            createStoreApplication(seller, "store1", StoreApprovalStatus.PENDING);
            createStoreApplication(seller, "store2", StoreApprovalStatus.APPROVE);
            createStoreApplication(seller, "store3", StoreApprovalStatus.PENDING);

            em.flush();
            em.clear();

            // when
            List<SellerApplicationInfo> result = repository.findSellerApplications(0, 10);

            // then
            assertThat(result).hasSize(2);

            for (SellerApplicationInfo app : result) {
                assertThat(app.sellerInfo().accountNumber()).isEqualTo("333");
            }

            assertThat(result)
                .extracting(r -> r.sellerStoreInfo().storeName())
                .containsExactlyInAnyOrder("store1", "store3");
            assertThat(result.get(0).storeApplicationId()).isLessThan(result.get(1).storeApplicationId());
        }

        @Test
        @DisplayName("첫 페이지를 조회한다.")
        void success_findSellerApplications_paging() {

            // given
            Seller seller = createSeller();
            createAccountVerification(seller, "123", true);

            for (int i = 1; i <= 5; i++) {
                createStoreApplication(seller, "store" + i, StoreApprovalStatus.PENDING);
            }

            em.flush();
            em.clear();

            // when
            List<SellerApplicationInfo> result = repository.findSellerApplications(0, 2);

            // then
            assertThat(result).hasSize(2);
            for (SellerApplicationInfo app : result) {
                assertThat(app.sellerInfo().accountNumber()).isEqualTo("123");
            }
            assertThat(result.get(0).storeApplicationId()).isLessThan(result.get(1).storeApplicationId());
        }

        @Test
        @DisplayName("판매자의 계좌 정보가 존재하지 않으면 조회되지 않는다.")
        void findSellerApplications_verified_false() {

            // given
            Seller seller = createSeller();
            createStoreApplication(seller, "store1", StoreApprovalStatus.PENDING);

            em.flush();
            em.clear();

            // when
            List<SellerApplicationInfo> result = repository.findSellerApplications(0, 10);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("인증되지 않은 계좌면 조회되지 않는다")
        void not_verified_account() {

            Seller seller = createSeller();
            createAccountVerification(seller, "111", false);
            createStoreApplication(seller, "store1", StoreApprovalStatus.PENDING);

            em.flush();
            em.clear();

            List<SellerApplicationInfo> result = repository.findSellerApplications(0, 10);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("offset이 범위를 넘으면 빈 리스트 반환")
        void paging_out_of_range() {

            Seller seller = createSeller();
            createAccountVerification(seller, "123", true);

            for (int i = 1; i <= 3; i++) {
                createStoreApplication(seller, "store" + i, StoreApprovalStatus.PENDING);
            }

            em.flush();
            em.clear();

            List<SellerApplicationInfo> result = repository.findSellerApplications(10, 5);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countSellerApplications() 테스트")
    class CountSellerApplicationsTest {

        private Seller createSeller() {
            Seller seller = SellerFixture.defaultSeller();
            em.persist(seller);
            return seller;
        }

        private void createAccountVerification(Seller seller, boolean verified) {
            AccountVerification av = AccountVerificationFixture.defaultAccountVerification(seller, "123", verified);
            em.persist(av);
        }

        private void createStoreApplication(Seller seller, StoreApprovalStatus status) {
            StoreApplication sa = StoreApplicationFixture.defaultStoreApplication("store", seller, status);
            em.persist(sa);
        }

        @Test
        @DisplayName("승인 대기 중이며 계좌 인증된 데이터만 카운트된다")
        void countSellerApplications() {

            // given
            Seller seller1 = createSeller();
            Seller seller2 = createSeller();

            createAccountVerification(seller1, true);
            createAccountVerification(seller2, true);

            createStoreApplication(seller1, StoreApprovalStatus.PENDING);
            createStoreApplication(seller2, StoreApprovalStatus.PENDING);

            // 제외 케이스
            createStoreApplication(seller1, StoreApprovalStatus.APPROVE);

            em.flush();
            em.clear();

            // when
            long count = repository.countSellerApplications();

            // then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("미인증 계좌 정보는 카운트되지 않는다")
        void countSellerApplications_exclude_not_verified() {

            // given
            Seller seller = createSeller();

            createAccountVerification(seller, false);
            createStoreApplication(seller, StoreApprovalStatus.PENDING);

            em.flush();
            em.clear();

            // when
            long count = repository.countSellerApplications();

            // then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("승인 대기 중인 상태가 아닌 경우 카운트되지 않는다")
        void countSellerApplications_exclude_not_pending() {

            // given
            Seller seller = createSeller();

            createAccountVerification(seller, true);
            createStoreApplication(seller, StoreApprovalStatus.APPROVE);

            em.flush();
            em.clear();

            // when
            long count = repository.countSellerApplications();

            // then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("데이터가 없으면 0을 반환한다")
        void countSellerApplications_empty() {

            // when
            long count = repository.countSellerApplications();

            // then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("여러 판매자 중 조건을 만족하는 것만 카운트된다")
        void countSellerApplications_sellers() {

            // given
            Seller seller1 = createSeller();
            Seller seller2 = createSeller();
            Seller seller3 = createSeller();

            createAccountVerification(seller1, true);
            createAccountVerification(seller2, false);
            createAccountVerification(seller3, true);

            createStoreApplication(seller1, StoreApprovalStatus.PENDING); // 포함
            createStoreApplication(seller2, StoreApprovalStatus.PENDING); // 제외 (verified=false)
            createStoreApplication(seller3, StoreApprovalStatus.APPROVE); // 제외 (status)

            em.flush();
            em.clear();

            // when
            long count = repository.countSellerApplications();

            // then
            assertThat(count).isEqualTo(1);
        }
    }
}