package com.bbangle.bbangle.vat.seller.excel.controller;

import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.vat.seller.controller.dto.request.SellerVatExcelType;
import com.bbangle.bbangle.vat.seller.controller.dto.request.SellerVatSearchRequest;
import com.bbangle.bbangle.vat.seller.excel.service.SellerVatExcelService;
import com.bbangle.bbangle.vat.seller.service.model.SellerVatCommand.SellerVatSearchCommand;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/vat/excel")
public class SellerVatExcelController implements SellerVatExcelApi {

    private static final long DUMMY_SELLER_ID = 1L;
    private static final String XLSX_CONTENT_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final SellerVatExcelService sellerVatExcelService;

    @Override
    @GetMapping(produces = XLSX_CONTENT_TYPE)
    public void downloadSellerVatExcel(
        SellerVatSearchRequest request,
        @RequestParam String type,
        @AuthenticationPrincipal Long sellerId,
        HttpServletResponse response
    ) throws IOException {
        SellerVatExcelType excelType = SellerVatExcelType.from(type);
        SellerVatSearchCommand command = request.toCommand(resolveSellerId(sellerId));

        response.setContentType(XLSX_CONTENT_TYPE);
        response.setHeader("Content-Disposition", buildContentDisposition(request, excelType));
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        sellerVatExcelService.writeExcel(command, excelType, response.getOutputStream());
        response.getOutputStream().flush();
    }

    private Long resolveSellerId(Long sellerId) {
        return sellerId == null ? DUMMY_SELLER_ID : sellerId;
    }

    private String buildContentDisposition(SellerVatSearchRequest request, SellerVatExcelType type) {
        String filename = String.format(
            "seller_vat_%s_%s_%s.xlsx",
            request.startMonth(),
            request.endMonth(),
            type.name()
        );
        return "attachment; filename=\"" + filename + "\"";
    }
}
