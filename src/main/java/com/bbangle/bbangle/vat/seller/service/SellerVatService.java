package com.bbangle.bbangle.vat.seller.service;

import com.bbangle.bbangle.vat.seller.controller.dto.response.SellerVatMonthlyItem;
import com.bbangle.bbangle.vat.seller.controller.dto.response.SellerVatSummaryResponse;
import com.bbangle.bbangle.vat.seller.provider.SellerVatSettlementProvider;
import com.bbangle.bbangle.vat.seller.provider.dto.SellerVatSettlementRow;
import com.bbangle.bbangle.vat.seller.service.model.SellerVatAmountSum;
import com.bbangle.bbangle.vat.seller.service.model.SellerVatCommand.SellerVatSearchCommand;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerVatService {

    private final SellerVatSettlementProvider settlementProvider;

    /**
     * 정산 row 단위 데이터를 월별로 합산해 화면 조회 응답으로 변환한다.
     * 같은 달에 여러 정산 건이 있으면 금액 컬럼을 모두 합산하고 최신 월부터 반환한다.
     */
    public SellerVatSummaryResponse getVatSummary(SellerVatSearchCommand command) {
        List<SellerVatSettlementRow> rows = settlementProvider.findSettlements(
            command.sellerId(),
            command.startMonth(),
            command.endMonth()
        );

        Map<YearMonth, SellerVatAmountSum> monthlySums = new TreeMap<>(Comparator.reverseOrder());
        for (SellerVatSettlementRow row : rows) {
            YearMonth settlementMonth = YearMonth.from(row.settlementDate());
            monthlySums.computeIfAbsent(settlementMonth, ignored -> new SellerVatAmountSum())
                .add(row);
        }

        List<SellerVatMonthlyItem> items = monthlySums.entrySet()
            .stream()
            .map(entry -> entry.getValue().toMonthlyItem(entry.getKey()))
            .toList();

        return new SellerVatSummaryResponse(
            command.startMonth().toString(),
            command.endMonth().toString(),
            items
        );
    }
}
