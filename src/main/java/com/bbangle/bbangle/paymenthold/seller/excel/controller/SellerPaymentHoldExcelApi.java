package com.bbangle.bbangle.paymenthold.seller.excel.controller;

import com.bbangle.bbangle.paymenthold.seller.controller.dto.request.PaymentHoldFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Tag(name = "Seller Payment Hold Excel", description = "(판매자) 지급보류 엑셀 API")
public interface SellerPaymentHoldExcelApi {

    @Operation(summary = "(판매자) 지급보류 엑셀 다운로드")
    void downloadPaymentHoldExcel(
        PaymentHoldFilter filter,
        @Parameter(hidden = true) Long sellerId,
        @Parameter(hidden = true) HttpServletResponse response
    ) throws IOException;
}
