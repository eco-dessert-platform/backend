package com.bbangle.bbangle.vat.seller.service.model;

import java.time.YearMonth;
import lombok.Builder;

public class SellerVatCommand {

    @Builder
    public record SellerVatSearchCommand(
        Long sellerId,
        YearMonth startMonth,
        YearMonth endMonth
    ) {
    }
}
