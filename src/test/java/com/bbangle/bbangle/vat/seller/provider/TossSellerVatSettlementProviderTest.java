package com.bbangle.bbangle.vat.seller.provider;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.vat.seller.provider.dto.SellerVatSettlementRow;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[Provider] TossSellerVatSettlementProvider")
class TossSellerVatSettlementProviderTest {

    private final TossSellerVatSettlementProvider provider = new TossSellerVatSettlementProvider();

    @Test
    @DisplayName("Toss 연동 전 임시 정산 데이터를 조회 월 범위에 맞춰 반환한다")
    void findSettlements_returnsFakeRowsWithinMonthRange() {
        // when
        List<SellerVatSettlementRow> rows = provider.findSettlements(
            1L,
            YearMonth.of(2025, 3),
            YearMonth.of(2025, 4)
        );

        // then
        assertThat(rows).hasSize(3);
        assertThat(rows).extracting(SellerVatSettlementRow::settlementNo)
            .containsExactly(
                "SETTLE-202504-0001",
                "SETTLE-202504-0002",
                "SETTLE-202503-0001"
            );
    }

    @Test
    @DisplayName("sellerId가 다르면 임시 데이터도 반환하지 않는다")
    void findSettlements_returnsEmptyRowsForDifferentSeller() {
        // when
        List<SellerVatSettlementRow> rows = provider.findSettlements(
            2L,
            YearMonth.of(2025, 3),
            YearMonth.of(2025, 4)
        );

        // then
        assertThat(rows).isEmpty();
    }
}
