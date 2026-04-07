package com.bbangle.bbangle.settlement.seller.excel.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.fixture.settlement.domain.DailySettlementFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.settlement.domain.DailySettlement;
import com.bbangle.bbangle.settlement.repository.DailySettlementRepository;
import com.bbangle.bbangle.settlement.seller.excel.service.model.DailySettlementExcelSearchCommand;
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
import org.springframework.test.context.ActiveProfiles;

@DisplayName("[Repository] DailySettlementExcelDSLRepository")
@Import({TestContainersConfig.class, QueryDslConfig.class, SearchFilter.class, SearchSort.class, DailySettlementExcelDSLRepositoryImpl.class})
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DailySettlementExcelDSLRepositoryImplTest {

    @Autowired
    private DailySettlementExcelDSLRepository excelRepository;

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

        // 다른 판매자 데이터 2건 (격리 검증용)
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
    @DisplayName("findAllForExcel - 엑셀 전체 조회")
    class FindAllForExcelTest {

        @Test
        @DisplayName("판매자 본인 데이터만 조회된다")
        void 판매자_본인_데이터만_조회된다() {
            // given
            DailySettlementExcelSearchCommand command = DailySettlementExcelSearchCommand.builder()
                .sellerId(seller.getId())
                .build();

            // when
            List<DailySettlement> result = excelRepository.findAllForExcel(command);

            // then
            assertThat(result).hasSize(5);
            assertThat(result).allMatch(ds -> ds.getSettlementNumber().startsWith("SETTLE-"));
        }

        @Test
        @DisplayName("startDate를 지정하면 해당 날짜 이후 데이터만 조회된다")
        void startDate_지정시_해당_날짜_이후_데이터만_조회된다() {
            // given
            DailySettlementExcelSearchCommand command = DailySettlementExcelSearchCommand.builder()
                .sellerId(seller.getId())
                .startDate(LocalDate.of(2025, 3, 3))
                .build();

            // when
            List<DailySettlement> result = excelRepository.findAllForExcel(command);

            // then - 3/3, 3/4, 3/5 총 3건
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("endDate를 지정하면 해당 날짜 이전 데이터만 조회된다")
        void endDate_지정시_해당_날짜_이전_데이터만_조회된다() {
            // given
            DailySettlementExcelSearchCommand command = DailySettlementExcelSearchCommand.builder()
                .sellerId(seller.getId())
                .endDate(LocalDate.of(2025, 3, 2))
                .build();

            // when
            List<DailySettlement> result = excelRepository.findAllForExcel(command);

            // then - 3/1, 3/2 총 2건
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("startDate와 endDate를 함께 지정하면 해당 기간 데이터만 조회된다")
        void 날짜_범위_모두_지정시_해당_기간_데이터만_조회된다() {
            // given
            DailySettlementExcelSearchCommand command = DailySettlementExcelSearchCommand.builder()
                .sellerId(seller.getId())
                .startDate(LocalDate.of(2025, 3, 2))
                .endDate(LocalDate.of(2025, 3, 4))
                .build();

            // when
            List<DailySettlement> result = excelRepository.findAllForExcel(command);

            // then - 3/2, 3/3, 3/4 총 3건
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("날짜 조건이 없으면 판매자의 모든 데이터가 조회된다")
        void 날짜_조건_없으면_전체_조회된다() {
            // given
            DailySettlementExcelSearchCommand command = DailySettlementExcelSearchCommand.builder()
                .sellerId(seller.getId())
                .build();

            // when
            List<DailySettlement> result = excelRepository.findAllForExcel(command);

            // then
            assertThat(result).hasSize(5);
        }

        @Test
        @DisplayName("다른 판매자의 데이터는 포함되지 않는다")
        void 다른_판매자_데이터는_포함되지_않는다() {
            // given
            DailySettlementExcelSearchCommand command = DailySettlementExcelSearchCommand.builder()
                .sellerId(seller.getId())
                .build();

            // when
            List<DailySettlement> result = excelRepository.findAllForExcel(command);

            // then
            assertThat(result).noneMatch(ds -> ds.getSettlementNumber().startsWith("OTHER-"));
        }

        @Test
        @DisplayName("조건에 맞는 데이터가 없으면 빈 리스트를 반환한다 (null 아님)")
        void 조건에_맞는_데이터가_없으면_빈_리스트_반환() {
            // given
            DailySettlementExcelSearchCommand command = DailySettlementExcelSearchCommand.builder()
                .sellerId(seller.getId())
                .startDate(LocalDate.of(2025, 4, 1))  // 데이터 없는 날짜
                .endDate(LocalDate.of(2025, 4, 30))
                .build();

            // when
            List<DailySettlement> result = excelRepository.findAllForExcel(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("결과가 id 내림차순으로 정렬된다")
        void 결과가_id_내림차순으로_정렬된다() {
            // given
            DailySettlementExcelSearchCommand command = DailySettlementExcelSearchCommand.builder()
                .sellerId(seller.getId())
                .build();

            // when
            List<DailySettlement> result = excelRepository.findAllForExcel(command);

            // then - id는 삽입 순서대로 증가하므로, 내림차순이면 마지막 삽입 데이터(SETTLE-5)가 먼저 온다
            assertThat(result).isNotEmpty();
            for (int i = 0; i < result.size() - 1; i++) {
                assertThat(result.get(i).getId())
                    .isGreaterThan(result.get(i + 1).getId());
            }
        }
    }

}
