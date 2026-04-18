package com.bbangle.bbangle.settlement.seller.service.model;

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
     * baseDate 기준 날짜 필터와 페이지네이션 정보를 포함한다.
     */
    @Builder
    public record SettlementItemSearchCommand(
        Long sellerId,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    ) {

    }

}
