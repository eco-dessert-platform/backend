package com.bbangle.bbangle.settlement.seller.excel.controller;

import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.settlement.seller.controller.dto.request.SettlementItemFilter;
import com.bbangle.bbangle.settlement.seller.excel.service.SellerSettlementItemExcelService;
import com.bbangle.bbangle.settlement.seller.excel.service.model.SettlementItemExcelSearchCommand;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 판매자 건별 정산 내역 엑셀 다운로드 컨트롤러.
 * GET /api/v1/seller/settlements/items/excel
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/settlements/items/excel")
public class SellerSettlementItemExcelController implements SellerSettlementItemExcelApi {

    private static final String XLSX_CONTENT_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final SellerSettlementItemExcelService excelService;

    /**
     * 건별 정산 내역 엑셀 다운로드.
     * 날짜 범위(startDate~endDate, 최대 1개월)가 필수이며, 조건에 맞는 전체 데이터를 xlsx로 반환한다.
     */
    @Override
    @GetMapping(produces = XLSX_CONTENT_TYPE)
    public void downloadSettlementItemExcel(
        SettlementItemFilter filter,
        @AuthenticationPrincipal Long sellerId,
        HttpServletResponse response
    ) throws IOException {
        // toExcelCommand() 내부에서 validateForExcel()을 호출하여 날짜 필수 + 최대 1개월 범위를 강제한다.
        SettlementItemExcelSearchCommand command = filter.toExcelCommand(sellerId);

        response.setContentType(XLSX_CONTENT_TYPE);
        response.setHeader("Content-Disposition", buildContentDisposition(filter.startDate(), filter.endDate()));
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        excelService.writeSettlementItemExcel(command, response.getOutputStream());
        response.getOutputStream().flush();
    }

    /**
     * 다운로드 파일명을 생성한다.
     * 형식: settlement-items_{startDate}_{endDate}.xlsx
     * (한글 파일명 깨짐 방지를 위해 영문만 사용)
     */
    private String buildContentDisposition(LocalDate startDate, LocalDate endDate) {
        String filename = String.format("settlement-items_%s_%s.xlsx", startDate, endDate);
        return "attachment; filename=\"" + filename + "\"";
    }

}
