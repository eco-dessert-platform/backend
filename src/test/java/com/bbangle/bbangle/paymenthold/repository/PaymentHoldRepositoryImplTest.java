package com.bbangle.bbangle.paymenthold.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.paymenthold.domain.PaymentHold;
import com.bbangle.bbangle.paymenthold.domain.model.PaymentHoldDateType;
import com.bbangle.bbangle.paymenthold.domain.model.PaymentHoldStatus;
import com.bbangle.bbangle.paymenthold.seller.excel.service.model.PaymentHoldExcelSearchCommand;
import com.bbangle.bbangle.paymenthold.seller.service.model.PaymentHoldSearchCommand;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.settlement.domain.SettlementItem;
import com.bbangle.bbangle.settlement.domain.model.SettlementStatus;
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

@DisplayName("[Repository] PaymentHoldRepository")
@Import({TestContainersConfig.class, QueryDslConfig.class, SearchFilter.class, SearchSort.class})
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentHoldRepositoryImplTest {

    @Autowired
    private PaymentHoldRepository paymentHoldRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    private Seller seller;
    private Seller otherSeller;
    private SettlementItem settlementItem1;
    private SettlementItem settlementItem2;
    private SettlementItem settlementItem3;
    private SettlementItem settlementItem4;

    @BeforeEach
    void setUp() {
        Store store = storeRepository.save(StoreFixture.defaultStore());
        Store otherStore = storeRepository.save(StoreFixture.defaultStore("other-store"));
        seller = sellerRepository.save(SellerFixture.createDefaultSeller(store));
        otherSeller = sellerRepository.save(SellerFixture.defaultSeller("other-seller", otherStore));

        settlementItem1 = persistSettlementItem("SETTLE-001", LocalDate.of(2025, 3, 1), null, "10000");
        settlementItem2 = persistSettlementItem("SETTLE-002", LocalDate.of(2025, 3, 2), LocalDate.of(2025, 3, 5), "20000");
        settlementItem3 = persistSettlementItem("SETTLE-003", LocalDate.of(2025, 3, 4), LocalDate.of(2025, 3, 7), "30000");
        settlementItem4 = persistSettlementItem("OTHER-001", LocalDate.of(2025, 3, 2), null, "40000");

        paymentHoldRepository.save(createPaymentHold(
            seller,
            settlementItem1,
            "SETTLE-001",
            PaymentHoldStatus.ON_HOLD,
            LocalDate.of(2025, 3, 1),
            null,
            new BigDecimal("10000")
        ));
        paymentHoldRepository.save(createPaymentHold(
            seller,
            settlementItem2,
            "SETTLE-002",
            PaymentHoldStatus.RELEASED,
            LocalDate.of(2025, 3, 2),
            LocalDate.of(2025, 3, 5),
            new BigDecimal("20000")
        ));
        paymentHoldRepository.save(createPaymentHold(
            seller,
            settlementItem3,
            "SETTLE-003",
            PaymentHoldStatus.ON_HOLD,
            LocalDate.of(2025, 3, 4),
            LocalDate.of(2025, 3, 7),
            new BigDecimal("30000")
        ));
        paymentHoldRepository.save(createPaymentHold(
            otherSeller,
            settlementItem4,
            "OTHER-001",
            PaymentHoldStatus.ON_HOLD,
            LocalDate.of(2025, 3, 2),
            null,
            new BigDecimal("40000")
        ));

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("searchPaymentHolds")
    class SearchPaymentHoldsTest {

        @Test
        @DisplayName("판매자, 상태, 기준일 조건으로 지급보류 목록을 조회한다")
        void searchBySellerStatusAndBaseDate() {
            PaymentHoldSearchCommand command = PaymentHoldSearchCommand.builder()
                .sellerId(seller.getId())
                .startDate(LocalDate.of(2025, 3, 1))
                .endDate(LocalDate.of(2025, 3, 4))
                .dateType(PaymentHoldDateType.BASE_DATE)
                .status(PaymentHoldStatus.ON_HOLD)
                .pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
                .build();

            Page<PaymentHold> result = paymentHoldRepository.searchPaymentHolds(command);

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                .extracting(PaymentHold::getSettlementNumber)
                .containsExactly("SETTLE-003", "SETTLE-001");
        }

        @Test
        @DisplayName("정산완료일 기준 조회는 completedDate를 사용한다")
        void searchByCompletedDate() {
            PaymentHoldSearchCommand command = PaymentHoldSearchCommand.builder()
                .sellerId(seller.getId())
                .startDate(LocalDate.of(2025, 3, 5))
                .endDate(LocalDate.of(2025, 3, 5))
                .dateType(PaymentHoldDateType.COMPLETED_DATE)
                .pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
                .build();

            Page<PaymentHold> result = paymentHoldRepository.searchPaymentHolds(command);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getSettlementNumber()).isEqualTo("SETTLE-002");
        }

        @Test
        @DisplayName("지급보류 ID 검색 시 기간 조건을 무시한다")
        void ignoreDateRangeWhenSearchingByPaymentHoldId() {
            Long paymentHoldId = paymentHoldRepository.findAll().stream()
                .filter(paymentHold -> paymentHold.getSettlementNumber().equals("SETTLE-001"))
                .findFirst()
                .orElseThrow()
                .getId();

            PaymentHoldSearchCommand command = PaymentHoldSearchCommand.builder()
                .sellerId(seller.getId())
                .paymentHoldId(paymentHoldId)
                .startDate(LocalDate.of(2025, 4, 1))
                .endDate(LocalDate.of(2025, 4, 30))
                .dateType(PaymentHoldDateType.BASE_DATE)
                .pageable(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")))
                .build();

            Page<PaymentHold> result = paymentHoldRepository.searchPaymentHolds(command);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(paymentHoldId);
        }
    }

    @Nested
    @DisplayName("findAllForExcel")
    class FindAllForExcelTest {

        @Test
        @DisplayName("정산 ID 검색 시 기간과 무관하게 엑셀 대상을 조회한다")
        void findAllForExcelBySettlementIdIgnoringDateRange() {
            PaymentHoldExcelSearchCommand command = PaymentHoldExcelSearchCommand.builder()
                .sellerId(seller.getId())
                .settlementId("SETTLE-002")
                .startDate(LocalDate.of(2025, 4, 1))
                .endDate(LocalDate.of(2025, 4, 30))
                .dateType(PaymentHoldDateType.COMPLETED_DATE)
                .build();

            List<PaymentHold> result = paymentHoldRepository.findAllForExcel(command);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSettlementNumber()).isEqualTo("SETTLE-002");
            assertThat(result.get(0).getSettlementAmount()).isEqualByComparingTo("20000");
        }
    }

    private PaymentHold createPaymentHold(
        Seller seller,
        SettlementItem settlementItem,
        String settlementNumber,
        PaymentHoldStatus status,
        LocalDate baseDate,
        LocalDate completedDate,
        BigDecimal settlementAmount
    ) {
        return PaymentHold.builder()
            .seller(seller)
            .settlementItem(settlementItem)
            .settlementNumber(settlementNumber)
            .status(status)
            .baseDate(baseDate)
            .completedDate(completedDate)
            .settlementAmount(settlementAmount)
            .build();
    }

    private SettlementItem persistSettlementItem(
        String settlementNumber,
        LocalDate baseDate,
        LocalDate completedDate,
        String scheduledAmount
    ) {
        SettlementItem settlementItem = SettlementItem.builder()
            .settlementNumber(settlementNumber)
            .type("NORMAL")
            .scheduledAmount(new BigDecimal(scheduledAmount))
            .baseDate(baseDate)
            .scheduledDate(baseDate.plusDays(7))
            .completedDate(completedDate)
            .status(SettlementStatus.PENDING)
            .build();
        em.persist(settlementItem);
        return settlementItem;
    }
}
