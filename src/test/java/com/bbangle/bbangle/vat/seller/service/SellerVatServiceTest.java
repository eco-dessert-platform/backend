package com.bbangle.bbangle.vat.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.bbangle.bbangle.vat.seller.controller.dto.response.SellerVatSummaryResponse;
import com.bbangle.bbangle.vat.seller.provider.SellerVatSettlementProvider;
import com.bbangle.bbangle.vat.seller.provider.dto.SellerVatSettlementRow;
import com.bbangle.bbangle.vat.seller.service.model.SellerVatCommand.SellerVatSearchCommand;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("[Service] SellerVatService")
class SellerVatServiceTest {

    @Mock
    private SellerVatSettlementProvider settlementProvider;

    @InjectMocks
    private SellerVatService sellerVatService;

    @Test
    @DisplayName("정산 row를 월별로 합산하고 최신 월부터 반환한다")
    void getVatSummary_aggregatesRowsByMonthDescending() {
        // given
        Long sellerId = 1L;
        YearMonth startMonth = YearMonth.of(2025, 3);
        YearMonth endMonth = YearMonth.of(2025, 4);

        given(settlementProvider.findSettlements(sellerId, startMonth, endMonth))
            .willReturn(List.of(
                row(LocalDate.of(2025, 4, 5), "SETTLE-202504-0001", 800000L, 100000L),
                row(LocalDate.of(2025, 4, 18), "SETTLE-202504-0002", 1075000L, 220000L),
                row(LocalDate.of(2025, 3, 9), "SETTLE-202503-0001", 1640000L, 210000L)
            ));

        SellerVatSearchCommand command = SellerVatSearchCommand.builder()
            .sellerId(sellerId)
            .startMonth(startMonth)
            .endMonth(endMonth)
            .build();

        // when
        SellerVatSummaryResponse response = sellerVatService.getVatSummary(command);

        // then
        assertThat(response.startMonth()).isEqualTo("2025-03");
        assertThat(response.endMonth()).isEqualTo("2025-04");
        assertThat(response.items()).hasSize(2);

        assertThat(response.items().get(0).month()).isEqualTo("2025-04");
        assertThat(response.items().get(0).taxableSalesAmount()).isEqualTo(1875000L);
        assertThat(response.items().get(0).taxFreeSalesAmount()).isEqualTo(320000L);

        assertThat(response.items().get(1).month()).isEqualTo("2025-03");
        assertThat(response.items().get(1).taxableSalesAmount()).isEqualTo(1640000L);
    }

    @Test
    @DisplayName("정산 row가 없으면 빈 items를 반환한다")
    void getVatSummary_returnsEmptyItemsWhenNoRows() {
        // given
        Long sellerId = 1L;
        YearMonth startMonth = YearMonth.of(2024, 9);
        YearMonth endMonth = YearMonth.of(2024, 10);

        given(settlementProvider.findSettlements(sellerId, startMonth, endMonth))
            .willReturn(List.of());

        SellerVatSearchCommand command = SellerVatSearchCommand.builder()
            .sellerId(sellerId)
            .startMonth(startMonth)
            .endMonth(endMonth)
            .build();

        // when
        SellerVatSummaryResponse response = sellerVatService.getVatSummary(command);

        // then
        assertThat(response.items()).isEmpty();
    }

    private SellerVatSettlementRow row(
        LocalDate settlementDate,
        String settlementNo,
        Long taxableSalesAmount,
        Long taxFreeSalesAmount
    ) {
        return new SellerVatSettlementRow(
            1L,
            settlementDate,
            settlementNo,
            taxableSalesAmount,
            taxFreeSalesAmount,
            0L,
            0L,
            0L,
            0L
        );
    }
}
