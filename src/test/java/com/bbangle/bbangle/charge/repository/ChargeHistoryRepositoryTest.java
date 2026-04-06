package com.bbangle.bbangle.charge.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.charge.domain.ChargeHistory;
import com.bbangle.bbangle.charge.domain.enums.ChargeTransactionStatus;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.charge.domain.ChargeHistoryFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.seller.domain.Seller;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("[Repository] ChargeHistoryRepository")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChargeHistoryRepositoryTest {

    @Autowired
    private ChargeHistoryRepository sut;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void resetTable() {
        em.flush();
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE charge_history").executeUpdate();
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    @Nested
    @DisplayName("findBySellerIdAndBaseDateBetween")
    class FindBySellerIdAndBaseDateBetweenTest {

        @Test
        @DisplayName("판매자 ID와 날짜 범위로 거래내역을 정상 조회한다")
        void testFindBySellerIdAndBaseDateBetweenSuccess() {
            // given
            Seller seller = SellerFixture.defaultSeller();
            em.persist(seller);

            LocalDate startDate = LocalDate.of(2025, 3, 1);
            LocalDate endDate = LocalDate.of(2025, 3, 7);
            LocalDate targetDate = LocalDate.of(2025, 3, 5);

            ChargeHistory history = ChargeHistoryFixture.createAccumulate(seller, targetDate);
            em.persist(history);
            em.flush();
            em.clear();

            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<ChargeHistory> result = sut.findBySellerIdAndBaseDateBetween(
                seller.getId(),
                startDate,
                endDate,
                pageable
            );

            // then
            assertThat(result).isNotEmpty();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getSeller().getId()).isEqualTo(seller.getId());
            assertThat(result.getContent().get(0).getBaseDate()).isEqualTo(targetDate);
            assertThat(result.getContent().get(0).getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
        }

        @Test
        @DisplayName("거래내역이 없을 때 빈 페이지를 반환한다")
        void testFindBySellerIdAndBaseDateBetweenEmpty() {
            // given
            Seller seller = SellerFixture.defaultSeller();
            em.persist(seller);
            em.flush();
            em.clear();

            LocalDate startDate = LocalDate.of(2025, 3, 1);
            LocalDate endDate = LocalDate.of(2025, 3, 7);

            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<ChargeHistory> result = sut.findBySellerIdAndBaseDateBetween(
                seller.getId(),
                startDate,
                endDate,
                pageable
            );

            // then
            assertThat(result).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("날짜 범위 밖의 거래는 조회되지 않는다")
        void testFindBySellerIdAndBaseDateBetweenOutsideDateRange() {
            // given
            Seller seller = SellerFixture.defaultSeller();
            em.persist(seller);

            LocalDate inRangeDate = LocalDate.of(2025, 3, 5);
            LocalDate outsideRangeDate = LocalDate.of(2025, 2, 20);

            ChargeHistory historyInRange = ChargeHistoryFixture.createAccumulate(seller, inRangeDate);
            ChargeHistory historyOutsideRange = ChargeHistoryFixture.createDeduct(seller, outsideRangeDate);

            em.persist(historyInRange);
            em.persist(historyOutsideRange);
            em.flush();
            em.clear();

            LocalDate startDate = LocalDate.of(2025, 3, 1);
            LocalDate endDate = LocalDate.of(2025, 3, 7);
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<ChargeHistory> result = sut.findBySellerIdAndBaseDateBetween(
                seller.getId(),
                startDate,
                endDate,
                pageable
            );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getBaseDate()).isEqualTo(inRangeDate);
        }

        @Test
        @DisplayName("여러 판매자가 있을 때 특정 판매자의 거래만 조회한다")
        void testFindBySellerIdAndBaseDateBetweenWithMultipleSellers() {
            // given
            Seller seller1 = SellerFixture.defaultSeller();
            Seller seller2 = SellerFixture.defaultSeller();
            em.persist(seller1);
            em.persist(seller2);

            LocalDate targetDate = LocalDate.of(2025, 3, 5);

            ChargeHistory history1 = ChargeHistoryFixture.createAccumulate(seller1, targetDate);
            ChargeHistory history2 = ChargeHistoryFixture.createPending(seller2, targetDate);

            em.persist(history1);
            em.persist(history2);
            em.flush();
            em.clear();

            LocalDate startDate = LocalDate.of(2025, 3, 1);
            LocalDate endDate = LocalDate.of(2025, 3, 7);
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<ChargeHistory> result1 = sut.findBySellerIdAndBaseDateBetween(
                seller1.getId(),
                startDate,
                endDate,
                pageable
            );
            Page<ChargeHistory> result2 = sut.findBySellerIdAndBaseDateBetween(
                seller2.getId(),
                startDate,
                endDate,
                pageable
            );

            // then
            assertThat(result1).hasSize(1);
            assertThat(result1.getContent().get(0).getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
            assertThat(result1.getContent().get(0).getSeller().getId()).isEqualTo(seller1.getId());

            assertThat(result2).hasSize(1);
            assertThat(result2.getContent().get(0).getAmount()).isEqualByComparingTo(new BigDecimal("100000"));
            assertThat(result2.getContent().get(0).getSeller().getId()).isEqualTo(seller2.getId());
        }

        @Test
        @DisplayName("페이지네이션을 올바르게 적용한다")
        void testFindBySellerIdAndBaseDateBetweenWithPagination() {
            // given
            Seller seller = SellerFixture.defaultSeller();
            em.persist(seller);

            LocalDate baseDate = LocalDate.of(2025, 3, 1);
            for (int i = 0; i < 25; i++) {
                ChargeHistory history = ChargeHistoryFixture.createAccumulate(
                    seller, baseDate.plusDays(i % 30)
                );
                em.persist(history);
            }
            em.flush();
            em.clear();

            LocalDate startDate = LocalDate.of(2025, 3, 1);
            LocalDate endDate = LocalDate.of(2025, 3, 31);

            // when - 첫 번째 페이지
            Pageable pageableFirst = PageRequest.of(0, 10);
            Page<ChargeHistory> firstPage = sut.findBySellerIdAndBaseDateBetween(
                seller.getId(),
                startDate,
                endDate,
                pageableFirst
            );

            // when - 두 번째 페이지
            Pageable pageableSecond = PageRequest.of(1, 10);
            Page<ChargeHistory> secondPage = sut.findBySellerIdAndBaseDateBetween(
                seller.getId(),
                startDate,
                endDate,
                pageableSecond
            );

            // then
            assertThat(firstPage.getSize()).isEqualTo(10);
            assertThat(firstPage.getNumber()).isEqualTo(0);
            assertThat(firstPage.getTotalElements()).isEqualTo(25);
            assertThat(firstPage.getTotalPages()).isEqualTo(3);
            assertThat(firstPage.hasNext()).isTrue();

            assertThat(secondPage.getSize()).isEqualTo(10);
            assertThat(secondPage.getNumber()).isEqualTo(1);
            assertThat(secondPage.hasNext()).isTrue();
            assertThat(secondPage.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("정렬을 올바르게 적용한다")
        void testFindBySellerIdAndBaseDateBetweenWithSorting() {
            // given
            Seller seller = SellerFixture.defaultSeller();
            em.persist(seller);

            ChargeHistory history1 = ChargeHistoryFixture.createAccumulate(
                seller, LocalDate.of(2025, 3, 1)
            );

            ChargeHistory history2 = ChargeHistoryFixture.createAccumulate(
                seller, LocalDate.of(2025, 3, 5)
            );

            ChargeHistory history3 = ChargeHistoryFixture.createAccumulate(
                seller, LocalDate.of(2025, 3, 3)
            );

            em.persist(history1);
            em.persist(history2);
            em.persist(history3);
            em.flush();
            em.clear();

            LocalDate startDate = LocalDate.of(2025, 3, 1);
            LocalDate endDate = LocalDate.of(2025, 3, 7);

            // when - createdAt 역순 정렬 (기본 정렬)
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<ChargeHistory> result = sut.findBySellerIdAndBaseDateBetween(
                seller.getId(),
                startDate,
                endDate,
                pageable
            );

            // then - createdAt DESC 순서로 정렬되므로 생성 순서의 역순
            assertThat(result).hasSize(3);
            assertThat(result.getContent().get(0).getBaseDate()).isEqualTo(LocalDate.of(2025, 3, 3));
            assertThat(result.getContent().get(1).getBaseDate()).isEqualTo(LocalDate.of(2025, 3, 5));
            assertThat(result.getContent().get(2).getBaseDate()).isEqualTo(LocalDate.of(2025, 3, 1));
        }

        @Test
        @DisplayName("다양한 거래 상태를 조회한다")
        void testFindBySellerIdAndBaseDateBetweenWithDifferentStatuses() {
            // given
            Seller seller = SellerFixture.defaultSeller();
            em.persist(seller);

            LocalDate targetDate = LocalDate.of(2025, 3, 5);

            ChargeHistory completedHistory = ChargeHistoryFixture.createAccumulate(seller, targetDate);
            ChargeHistory pendingHistory = ChargeHistoryFixture.createPending(seller, targetDate);

            em.persist(completedHistory);
            em.persist(pendingHistory);
            em.flush();
            em.clear();

            LocalDate startDate = LocalDate.of(2025, 3, 1);
            LocalDate endDate = LocalDate.of(2025, 3, 7);
            Pageable pageable = PageRequest.of(0, 20);

            // when
            Page<ChargeHistory> result = sut.findBySellerIdAndBaseDateBetween(
                seller.getId(),
                startDate,
                endDate,
                pageable
            );

            // then
            assertThat(result).hasSize(2);
            assertThat(result.getContent())
                .extracting("status")
                .containsExactlyInAnyOrder(
                    ChargeTransactionStatus.COMPLETED,
                    ChargeTransactionStatus.PENDING
                );
        }
    }
}
