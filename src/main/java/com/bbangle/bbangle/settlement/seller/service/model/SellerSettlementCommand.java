package com.bbangle.bbangle.settlement.seller.service.model;

import com.bbangle.bbangle.settlement.domain.model.SettlementItemDateType;
import com.bbangle.bbangle.settlement.domain.model.SettlementItemSearchType;
import java.time.LocalDate;
import lombok.Builder;
import org.springframework.data.domain.Pageable;

public class SellerSettlementCommand {

    @Builder
    public record DailySettlementSearchCommand(
        Long sellerId,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    ) {

    }

    /**
     * 건별 정산 내역 페이지네이션 조회 커맨드.
     * dateType 기준 날짜 필터, 검색 조건, 페이지네이션 정보를 포함한다.
     */
    @Builder
    public record SettlementItemSearchCommand(
        Long sellerId,
        SettlementItemDateType dateType,
        LocalDate startDate,
        LocalDate endDate,
        SettlementItemSearchType searchType,
        String searchValue,
        Pageable pageable
    ) {

    }

}
