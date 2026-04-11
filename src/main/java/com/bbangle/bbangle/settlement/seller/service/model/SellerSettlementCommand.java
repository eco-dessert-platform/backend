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

}
