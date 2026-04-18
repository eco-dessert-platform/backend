package com.bbangle.bbangle.settlement.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.bbangle.bbangle.settlement.repository.DailySettlementRepository;
import com.bbangle.bbangle.settlement.repository.SettlementItemRepository;
import com.bbangle.bbangle.settlement.repository.dao.SettlementItemDetailDao;
import com.bbangle.bbangle.settlement.repository.dao.SettlementItemSummaryDao;
import com.bbangle.bbangle.settlement.domain.model.SettlementStatus;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.SettlementItemResponse.SettlementItemPageResponse;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.SettlementItemSearchCommand;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("[Service] SellerSettlementService - getSettlementItems")
class SellerSettlementServiceTest {

    @Mock
    private DailySettlementRepository dailySettlementRepository;

    @Mock
    private SettlementItemRepository settlementItemRepository;

    @InjectMocks
    private SellerSettlementService sellerSettlementService;

    private static final Long SELLER_ID = 1L;
    private static final Pageable PAGEABLE = PageRequest.of(0, 10);

    @Nested
    @DisplayName("getSettlementItems - 건별 정산내역 조회")
    class GetSettlementItemsTest {

        @Test
        @DisplayName("정상 조회 시 페이지 응답과 요약 정보가 반환된다")
        void 정상_조회시_페이지와_요약_반환() {
            // given
            SettlementItemDetailDao dao = new SettlementItemDetailDao(
                "ORDER-001",
                101L,
                SELLER_ID,
                "홍길동",
                "글루텐프리 식빵",
                new BigDecimal("25000"),
                2,
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 8),
                SettlementStatus.PENDING
            );

            Page<SettlementItemDetailDao> mockPage = new PageImpl<>(
                List.of(dao),
                PAGEABLE,
                1L
            );

            SettlementItemSummaryDao mockSummary = new SettlementItemSummaryDao(
                new BigDecimal("25000")
            );

            given(settlementItemRepository.searchSettlementItems(any())).willReturn(mockPage);
            given(settlementItemRepository.fetchSummary(any())).willReturn(mockSummary);

            SettlementItemSearchCommand command = SettlementItemSearchCommand.builder()
                .sellerId(SELLER_ID)
                .startDate(LocalDate.of(2025, 3, 1))
                .endDate(LocalDate.of(2025, 3, 31))
                .pageable(PAGEABLE)
                .build();

            // when
            SettlementItemPageResponse response = sellerSettlementService.getSettlementItems(command);

            // then
            assertThat(response).isNotNull();
            assertThat(response.settlements().content()).hasSize(1);
            assertThat(response.settlements().totalElements()).isEqualTo(1L);
            assertThat(response.settlements().page()).isZero();
            assertThat(response.settlements().size()).isEqualTo(10);
        }

        @Test
        @DisplayName("SettlementStatus가 PENDING이면 응답에 '정산대기' 한글 설명이 담긴다")
        void 정산상태_PENDING은_한글_설명으로_변환된다() {
            // given
            SettlementItemDetailDao dao = new SettlementItemDetailDao(
                "ORDER-001",
                101L,
                SELLER_ID,
                "구매자",
                "상품명",
                new BigDecimal("10000"),
                1,
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 8),
                SettlementStatus.PENDING
            );

            given(settlementItemRepository.searchSettlementItems(any()))
                .willReturn(new PageImpl<>(List.of(dao), PAGEABLE, 1));
            given(settlementItemRepository.fetchSummary(any()))
                .willReturn(new SettlementItemSummaryDao(new BigDecimal("10000")));

            SettlementItemSearchCommand command = SettlementItemSearchCommand.builder()
                .sellerId(SELLER_ID)
                .pageable(PAGEABLE)
                .build();

            // when
            SettlementItemPageResponse response = sellerSettlementService.getSettlementItems(command);

            // then - SettlementStatus.PENDING.getDescription() = "정산대기"
            assertThat(response.settlements().content().get(0).status()).isEqualTo("정산대기");
        }

        @Test
        @DisplayName("SettlementStatus가 BEFORE_PAYMENT_CANCELLED이면 응답에 '정산 전 취소' 설명이 담긴다")
        void 정산상태_BEFORE_PAYMENT_CANCELLED은_한글_설명으로_변환된다() {
            // given
            SettlementItemDetailDao dao = new SettlementItemDetailDao(
                "ORDER-002",
                102L,
                SELLER_ID,
                "구매자",
                "상품명",
                new BigDecimal("10000"),
                1,
                LocalDate.of(2025, 3, 2),
                LocalDate.of(2025, 3, 9),
                SettlementStatus.BEFORE_PAYMENT_CANCELLED
            );

            given(settlementItemRepository.searchSettlementItems(any()))
                .willReturn(new PageImpl<>(List.of(dao), PAGEABLE, 1));
            given(settlementItemRepository.fetchSummary(any()))
                .willReturn(new SettlementItemSummaryDao(new BigDecimal("10000")));

            SettlementItemSearchCommand command = SettlementItemSearchCommand.builder()
                .sellerId(SELLER_ID)
                .pageable(PAGEABLE)
                .build();

            // when
            SettlementItemPageResponse response = sellerSettlementService.getSettlementItems(command);

            // then
            assertThat(response.settlements().content().get(0).status()).isEqualTo("정산 전 취소");
        }

        @Test
        @DisplayName("DAO의 status가 null이면 응답의 status도 null이다")
        void status_null이면_응답도_null() {
            // given
            SettlementItemDetailDao dao = new SettlementItemDetailDao(
                "ORDER-003",
                103L,
                SELLER_ID,
                "구매자",
                "상품명",
                new BigDecimal("10000"),
                1,
                LocalDate.of(2025, 3, 3),
                LocalDate.of(2025, 3, 10),
                null  // status가 null인 경우
            );

            given(settlementItemRepository.searchSettlementItems(any()))
                .willReturn(new PageImpl<>(List.of(dao), PAGEABLE, 1));
            given(settlementItemRepository.fetchSummary(any()))
                .willReturn(new SettlementItemSummaryDao(new BigDecimal("10000")));

            SettlementItemSearchCommand command = SettlementItemSearchCommand.builder()
                .sellerId(SELLER_ID)
                .pageable(PAGEABLE)
                .build();

            // when
            SettlementItemPageResponse response = sellerSettlementService.getSettlementItems(command);

            // then
            assertThat(response.settlements().content().get(0).status()).isNull();
        }

        @Test
        @DisplayName("조회 결과가 없으면 빈 content와 총 건수 0이 반환된다")
        void 조회_결과_없으면_빈_응답_반환() {
            // given
            given(settlementItemRepository.searchSettlementItems(any()))
                .willReturn(new PageImpl<>(List.of(), PAGEABLE, 0));
            given(settlementItemRepository.fetchSummary(any()))
                .willReturn(new SettlementItemSummaryDao(BigDecimal.ZERO));

            SettlementItemSearchCommand command = SettlementItemSearchCommand.builder()
                .sellerId(SELLER_ID)
                .pageable(PAGEABLE)
                .build();

            // when
            SettlementItemPageResponse response = sellerSettlementService.getSettlementItems(command);

            // then
            assertThat(response.settlements().content()).isEmpty();
            assertThat(response.settlements().totalElements()).isZero();
            assertThat(response.summary().totalCount()).isZero();
            assertThat(response.summary().totalScheduledAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("요약 정보에 총 건수와 총 정산 예정 금액이 올바르게 담긴다")
        void 요약_정보_올바르게_담긴다() {
            // given
            given(settlementItemRepository.searchSettlementItems(any()))
                .willReturn(new PageImpl<>(List.of(), PAGEABLE, 42));
            given(settlementItemRepository.fetchSummary(any()))
                .willReturn(new SettlementItemSummaryDao(new BigDecimal("1050000")));

            SettlementItemSearchCommand command = SettlementItemSearchCommand.builder()
                .sellerId(SELLER_ID)
                .pageable(PAGEABLE)
                .build();

            // when
            SettlementItemPageResponse response = sellerSettlementService.getSettlementItems(command);

            // then
            assertThat(response.summary().totalCount()).isEqualTo(42L);
            assertThat(response.summary().totalScheduledAmount())
                .isEqualByComparingTo(new BigDecimal("1050000"));
        }

        @Test
        @DisplayName("settlementStartDate와 settlementEndDate 모두 baseDate 값으로 매핑된다")
        void 정산시작일과_정산종료일_모두_baseDate로_매핑된다() {
            // given
            LocalDate baseDate = LocalDate.of(2025, 3, 5);
            SettlementItemDetailDao dao = new SettlementItemDetailDao(
                "ORDER-004",
                104L,
                SELLER_ID,
                "구매자",
                "상품명",
                new BigDecimal("15000"),
                1,
                baseDate,
                baseDate.plusDays(7),
                SettlementStatus.PENDING
            );

            given(settlementItemRepository.searchSettlementItems(any()))
                .willReturn(new PageImpl<>(List.of(dao), PAGEABLE, 1));
            given(settlementItemRepository.fetchSummary(any()))
                .willReturn(new SettlementItemSummaryDao(new BigDecimal("15000")));

            SettlementItemSearchCommand command = SettlementItemSearchCommand.builder()
                .sellerId(SELLER_ID)
                .pageable(PAGEABLE)
                .build();

            // when
            SettlementItemPageResponse response = sellerSettlementService.getSettlementItems(command);

            // then — 시작일과 종료일 모두 baseDate와 동일해야 한다
            var summary = response.settlements().content().get(0);
            assertThat(summary.settlementStartDate()).isEqualTo(baseDate);
            assertThat(summary.settlementEndDate()).isEqualTo(baseDate);
        }

    } // end GetSettlementItemsTest

}
