package com.bbangle.bbangle.paymenthold.seller.excel.controller;

import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.paymenthold.seller.controller.dto.request.PaymentHoldFilter;
import com.bbangle.bbangle.paymenthold.seller.excel.service.SellerPaymentHoldExcelService;
import com.bbangle.bbangle.paymenthold.seller.excel.service.model.PaymentHoldExcelSearchCommand;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/payment-hold/excel")
public class SellerPaymentHoldExcelController implements SellerPaymentHoldExcelApi {

    private static final String XLSX_CONTENT_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final SellerPaymentHoldExcelService sellerPaymentHoldExcelService;

    @Override
    @GetMapping(produces = XLSX_CONTENT_TYPE)
    public void downloadPaymentHoldExcel(
        PaymentHoldFilter filter,
        @AuthenticationPrincipal Long sellerId,
        HttpServletResponse response
    ) throws IOException {
        PaymentHoldExcelSearchCommand command = filter.toExcelCommand(sellerId);
        sellerPaymentHoldExcelService.validateExcelDownload(command);

        response.setContentType(XLSX_CONTENT_TYPE);
        response.setHeader("Content-Disposition", buildContentDisposition(filter));
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        sellerPaymentHoldExcelService.writePaymentHoldExcel(command, response.getOutputStream());
        response.getOutputStream().flush();
    }

    private String buildContentDisposition(PaymentHoldFilter filter) {
        String filename;
        if (filter.paymentHoldId() != null) {
            filename = String.format("payment-hold_%s.xlsx", filter.paymentHoldId());
        } else if (filter.settlementId() != null && !filter.settlementId().isBlank()) {
            filename = String.format("payment-hold_%s.xlsx", filter.settlementId());
        } else {
            LocalDate startDate = filter.startDate();
            LocalDate endDate = filter.endDate();
            filename = String.format("payment-hold_%s_%s.xlsx", startDate, endDate);
        }
        return "attachment; filename=\"" + filename + "\"";
    }
}
