package com.bbangle.bbangle.vat.seller.controller.dto.request;

import com.bbangle.bbangle.vat.seller.service.model.SellerVatCommand.SellerVatSearchCommand;
import com.bbangle.bbangle.vat.seller.util.SellerVatMonthValidator;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.YearMonth;

public record SellerVatSearchRequest(
    @Parameter(description = "조회 시작 월(yyyy-MM)", example = "2024-09")
    String startMonth,

    @Parameter(description = "조회 종료 월(yyyy-MM)", example = "2025-04")
    String endMonth
) {

    public SellerVatSearchCommand toCommand(Long sellerId) {
        YearMonth parsedStartMonth = SellerVatMonthValidator.parse(startMonth);
        YearMonth parsedEndMonth = SellerVatMonthValidator.parse(endMonth);
        SellerVatMonthValidator.validateRange(parsedStartMonth, parsedEndMonth);

        return SellerVatSearchCommand.builder()
            .sellerId(sellerId)
            .startMonth(parsedStartMonth)
            .endMonth(parsedEndMonth)
            .build();
    }
}
