package com.bbangle.bbangle.vat.seller.provider;

import com.bbangle.bbangle.vat.seller.provider.dto.SellerVatSettlementRow;
import java.time.YearMonth;
import java.util.List;

public interface SellerVatSettlementProvider {

    /**
     * 판매자 부가세 신고 자료의 원천 정산 데이터를 조회한다.
     * 현재 구현체는 Toss 연동 전 임시 데이터를 반환하고, 이후 Toss API 조회 로직으로 교체된다.
     */
    List<SellerVatSettlementRow> findSettlements(
        Long sellerId,
        YearMonth startMonth,
        YearMonth endMonth
    );
}
