package com.bbangle.bbangle.vat.seller.provider;

import com.bbangle.bbangle.vat.seller.provider.dto.SellerVatSettlementRow;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TossSellerVatSettlementProvider implements SellerVatSettlementProvider {

    /**
     * Toss 정산 API 연동 전까지 사용하는 임시 정산 데이터.
     * 계좌 인증의 TossAccountVerificationClient처럼 실제 연동 지점은 유지하고 내부 응답만 임시값으로 둔다.
     */
    private static final List<SellerVatSettlementRow> FAKE_ROWS = List.of(
        new SellerVatSettlementRow(
            1L,
            LocalDate.of(2025, 4, 5),
            "SETTLE-202504-0001",
            800000L,
            100000L,
            500000L,
            180000L,
            90000L,
            130000L
        ),
        new SellerVatSettlementRow(
            1L,
            LocalDate.of(2025, 4, 18),
            "SETTLE-202504-0002",
            1075000L,
            220000L,
            750000L,
            250000L,
            170000L,
            125000L
        ),
        new SellerVatSettlementRow(
            1L,
            LocalDate.of(2025, 3, 9),
            "SETTLE-202503-0001",
            1640000L,
            210000L,
            1120000L,
            350000L,
            180000L,
            200000L
        )
    );

    @Override
    public List<SellerVatSettlementRow> findSettlements(
        Long sellerId,
        YearMonth startMonth,
        YearMonth endMonth
    ) {
        // TODO: Toss 정산 API 연동 시 실제 조회 로직으로 교체
        return FAKE_ROWS.stream()
            .filter(row -> row.sellerId().equals(sellerId))
            .filter(row -> {
                YearMonth settlementMonth = YearMonth.from(row.settlementDate());
                return !settlementMonth.isBefore(startMonth) && !settlementMonth.isAfter(endMonth);
            })
            .toList();
    }
}
