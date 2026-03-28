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
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplication;
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

            createAccountVerification(seller, "111", true);
            createAccountVerification(seller, "999", false); // 최신이지만 제외되어야 함
            createAccountVerification(seller, "333", true);  // 이게 선택되어야 함

            createStoreApplication(seller, "store1", StoreApprovalStatus.PENDING);
            createStoreApplication(seller, "store2", StoreApprovalStatus.APPROVE);
            createStoreApplication(seller, "store3", StoreApprovalStatus.PENDING);

            em.flush();
            em.clear();

            // when
            List<AdminSellerApplication> result = repository.findSellerApplications(0, 10);

            // then
            assertThat(result).hasSize(2);

            // ✅ 최신 verified=true 계좌 검증
            for (AdminSellerApplication app : result) {
                assertThat(app.sellerInfo().accountNumber()).isEqualTo("333");
            }

            // ✅ PENDING만 조회되는지
            assertThat(result)
                .extracting(r -> r.sellerStoreInfo().storeName())
                .containsExactlyInAnyOrder("store1", "store3");

            // ✅ 정렬 검증 (id 기반)
            Long firstId = result.get(0).storeApplicationId();
            Long secondId = result.get(1).storeApplicationId();

            assertThat(firstId).isLessThan(secondId);
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
            List<AdminSellerApplication> result = repository.findSellerApplications(0, 2);

            // then
            assertThat(result).hasSize(2);

            // ✅ id 기반 정렬 검증
            Long firstId = result.get(0).storeApplicationId();
            Long secondId = result.get(1).storeApplicationId();
            assertThat(firstId).isLessThan(secondId);
        }

        @Test
        @DisplayName("판매자의 계좌가 인증되지 않았으면 조회되지 않는다.")
        void findSellerApplications_verified_false() {

            // given
            Seller seller = createSeller();
            createAccountVerification(seller, "111", false);
            createStoreApplication(seller, "store1", StoreApprovalStatus.PENDING);

            em.flush();
            em.clear();

            // when
            List<AdminSellerApplication> result = repository.findSellerApplications(0, 10);

            // then
            assertThat(result).isEmpty(); // inner join 영향
        }
    }
}