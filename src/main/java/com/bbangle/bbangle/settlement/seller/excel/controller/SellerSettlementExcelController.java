package com.bbangle.bbangle.settlement.seller.excel.controller;

import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.settlement.seller.controller.dto.request.DailySettlementFilter;
import com.bbangle.bbangle.settlement.seller.excel.service.SellerSettlementExcelService;
import com.bbangle.bbangle.settlement.seller.excel.service.model.DailySettlementExcelSearchCommand;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 판매자 일별 정산 내역 엑셀 다운로드 컨트롤러.
 * GET /api/v1/seller/settlements/daily/excel
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/settlements/daily/excel")
public class SellerSettlementExcelController implements SellerSettlementExcelApi {

    private static final String XLSX_CONTENT_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final SellerSettlementExcelService excelService;

    @Override
    @GetMapping(produces = XLSX_CONTENT_TYPE)
    public void downloadDailySettlementExcel(
        DailySettlementFilter filter,
        @AuthenticationPrincipal Long sellerId,
        HttpServletResponse response
    ) throws IOException {
        // DailySettlementFilter의 날짜 범위 검증(validateDateRange)이 private이므로
        // toCommand()를 통해 간접 호출해 검증 로직을 재사용한다. (Pageable은 실제로 사용하지 않음)
        filter.toCommand(sellerId, Pageable.unpaged());

        DailySettlementExcelSearchCommand command = DailySettlementExcelSearchCommand.builder()
            .sellerId(sellerId)
            .startDate(filter.startDate())
            .endDate(filter.endDate())
            .build();

        response.setContentType(XLSX_CONTENT_TYPE);
        response.setHeader("Content-Disposition", buildContentDisposition(filter.startDate(), filter.endDate()));
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        excelService.writeDailySettlementExcel(command, response.getOutputStream());
        response.getOutputStream().flush();
    }

    /**
     * 다운로드 파일명을 생성한다.
     * 형식: daily-settlements_{startDate}_{endDate}.xlsx
     * 날짜가 null이면 "all"로 대체한다. (한글 파일명 깨짐 방지를 위해 영문만 사용)
     */
    private String buildContentDisposition(LocalDate startDate, LocalDate endDate) {
        String start = startDate != null ? startDate.toString() : "all";
        String end = endDate != null ? endDate.toString() : "all";
        String filename = String.format("daily-settlements_%s_%s.xlsx", start, end);
        return "attachment; filename=\"" + filename + "\"";
    }

}
