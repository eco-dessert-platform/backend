package com.bbangle.bbangle.settlement.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.settlement.domain.DailySettlementFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.settlement.domain.DailySettlement;
import com.bbangle.bbangle.settlement.repository.dao.DailySettlementSummaryDao;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.DailySettlementSearchCommand;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("[Repository] DailySettlementDSLRepository")
@Import({TestContainersConfig.class, QueryDslConfig.class, SearchFilter.class, SearchSort.class})
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DailySettlementDSLRepositoryImplTest {

    @Autowired
    private DailySettlementRepository dailySettlementRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    private Seller seller;
    private Seller otherSeller;

    @BeforeEach
    void setUp() {
        Store store = storeRepository.save(StoreFixture.defaultStore());
        Store otherStore = storeRepository.save(StoreFixture.defaultStore("other"));
        seller = sellerRepository.save(SellerFixture.createDefaultSeller(store));
        otherSeller = sellerRepository.save(SellerFixture.defaultSeller("다른 판매자", otherStore));

        // 테스트 판매자의 정산 데이터 5건 (3/1 ~ 3/5)
        for (int i = 1; i <= 5; i++) {
            dailySettlementRepository.save(
                DailySettlementFixture.create(
                    seller,
                    "SETTLE-" + i,
                    LocalDate.of(2025, 3, i),
                    new BigDecimal("100000"),
                    new BigDecimal("-3000"),
                    new BigDecimal("-2000"),
                    BigDecimal.ZERO
                )
            );
        }

        // 다른 판매자의 정산 데이터 2건 (격리 확인용)
        for (int i = 1; i <= 2; i++) {
            dailySettlementRepository.save(
                DailySettlementFixture.create(
                    otherSeller,
                    "OTHER-" + i,
                    LocalDate.of(2025, 3, i),
                    new BigDecimal("200000"),
                    new BigDecimal("-5000"),
                    new BigDecimal("-3000"),
                    BigDecimal.ZERO
                )
            );
        }

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("searchDailySettlements - 페이징 목록 조회")
    class SearchDailySettlementsTest {

        @Test
        @DisplayName("판매자 본인 데이터만 조회된다")
        void 판매자_본인_데이터만_조회된다() {
            // given
            DailySettlementSearchCommand command = DailySettlementSearchCommand.builder()
                .sellerId(seller.getId())
                .pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
                .build();

            // when
            Page<DailySettlement> result = dailySettlementRepository.searchDailySettlements(command);

            // then
            assertThat(result.getTotalElements()).isEqualTo(5);
            assertThat(result.getContent()).allMatch(
                ds -> ds.getSettlementNumber().startsWith("SETTLE-")
            );
        }

        @Test
        @DisplayName("날짜 범위 필터가 정상 동작한다")
        void 날짜_범위_필터가_정상_동작한다() {
            // given
            DailySettlementSearchCommand command = DailySettlementSearchCommand.builder()
                .sellerId(seller.getId())
                .startDate(LocalDate.of(2025, 3, 2))
                .endDate(LocalDate.of(2025, 3, 4))
                .pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
                .build();

            // when
            Page<DailySettlement> result = dailySettlementRepository.searchDailySettlements(command);

            // then
            assertThat(result.getTotalElements()).isEqualTo(3);
            assertThat(result.getContent()).allMatch(
                ds -> !ds.getScheduledDate().isBefore(LocalDate.of(2025, 3, 2))
                    && !ds.getScheduledDate().isAfter(LocalDate.of(2025, 3, 4))
            );
        }

        @Test
        @DisplayName("페이지네이션이 정상 동작한다")
        void 페이지네이션이_정상_동작한다() {
            // given
            DailySettlementSearchCommand command = DailySettlementSearchCommand.builder()
                .sellerId(seller.getId())
                .pageable(PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "id")))
                .build();

            // when
            Page<DailySettlement> firstPage = dailySettlementRepository.searchDailySettlements(command);

            DailySettlementSearchCommand secondCommand = DailySettlementSearchCommand.builder()
                .sellerId(seller.getId())
                .pageable(PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "id")))
                .build();
            Page<DailySettlement> secondPage = dailySettlementRepository.searchDailySettlements(secondCommand);

            // then
            assertThat(firstPage.getContent()).hasSize(2);
            assertThat(firstPage.getTotalPages()).isEqualTo(3);
            assertThat(secondPage.getContent()).hasSize(2);
            // id DESC 정렬이므로 첫 페이지 id가 두 번째 페이지보다 크다
            assertThat(firstPage.getContent().get(0).getId())
                .isGreaterThan(secondPage.getContent().get(0).getId());
        }

        @Test
        @DisplayName("날짜 필터 없이 조회하면 전체 데이터가 반환된다")
        void 날짜_필터_없이_조회하면_전체_데이터가_반환된다() {
            // given
            DailySettlementSearchCommand command = DailySettlementSearchCommand.builder()
                .sellerId(seller.getId())
                .pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
                .build();

            // when
            Page<DailySettlement> result = dailySettlementRepository.searchDailySettlements(command);

            // then
            assertThat(result.getTotalElements()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("fetchSummary - 요약 집계 조회")
    class FetchSummaryTest {

        @Test
        @DisplayName("정산예정일 범위와 총 정산금액이 정확히 집계된다")
        void 정산예정일_범위와_총_정산금액이_정확히_집계된다() {
            // given
            DailySettlementSearchCommand command = DailySettlementSearchCommand.builder()
                .sellerId(seller.getId())
                .startDate(LocalDate.of(2025, 3, 1))
                .endDate(LocalDate.of(2025, 3, 5))
                .pageable(PageRequest.of(0, 10))
                .build();

            // when
            DailySettlementSummaryDao summary = dailySettlementRepository.fetchSummary(command);

            // then
            assertThat(summary.scheduledDateMin()).isEqualTo(LocalDate.of(2025, 3, 1));
            assertThat(summary.scheduledDateMax()).isEqualTo(LocalDate.of(2025, 3, 5));

            // 각 행: 100000 + (-3000) + (-2000) + 0 = 95000, 5건 = 475000
            assertThat(summary.totalSettlementAmount())
                .isEqualByComparingTo(new BigDecimal("475000"));
        }

        @Test
        @DisplayName("다른 판매자의 데이터는 집계에 포함되지 않는다")
        void 다른_판매자_데이터는_집계에_포함되지_않는다() {
            // given
            DailySettlementSearchCommand command = DailySettlementSearchCommand.builder()
                .sellerId(seller.getId())
                .pageable(PageRequest.of(0, 10))
                .build();

            DailySettlementSearchCommand otherCommand = DailySettlementSearchCommand.builder()
                .sellerId(otherSeller.getId())
                .pageable(PageRequest.of(0, 10))
                .build();

            // when
            DailySettlementSummaryDao sellerSummary = dailySettlementRepository.fetchSummary(command);
            DailySettlementSummaryDao otherSummary = dailySettlementRepository.fetchSummary(otherCommand);

            // then
            // seller: 95000 * 5 = 475000
            assertThat(sellerSummary.totalSettlementAmount())
                .isEqualByComparingTo(new BigDecimal("475000"));
            // otherSeller: (200000 - 5000 - 3000) * 2 = 384000
            assertThat(otherSummary.totalSettlementAmount())
                .isEqualByComparingTo(new BigDecimal("384000"));
        }

        @Test
        @DisplayName("데이터가 없으면 총 정산금액은 0이다")
        void 데이터가_없으면_총_정산금액은_0이다() {
            // given
            DailySettlementSearchCommand command = DailySettlementSearchCommand.builder()
                .sellerId(seller.getId())
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 31))
                .pageable(PageRequest.of(0, 10))
                .build();

            // when
            DailySettlementSummaryDao summary = dailySettlementRepository.fetchSummary(command);

            // then
            assertThat(summary.scheduledDateMin()).isNull();
            assertThat(summary.scheduledDateMax()).isNull();
            assertThat(summary.totalSettlementAmount())
                .isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

}
