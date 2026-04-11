package com.bbangle.bbangle.settlement.seller.excel.controller;

import com.bbangle.bbangle.settlement.seller.controller.dto.request.SettlementItemFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springdoc.core.annotations.ParameterObject;

/**
 * 판매자 건별 정산 내역 엑셀 다운로드 API Swagger 인터페이스.
 */
@Tag(name = "Seller Settlement Excel", description = "(판매자) 정산 엑셀 API")
public interface SellerSettlementItemExcelApi {

    @Operation(summary = "(판매자) 건별 정산내역 엑셀 다운로드",
        description = "조회 조건(startDate, endDate)에 맞는 건별 정산 내역 전체를 엑셀(.xlsx)로 다운로드합니다.")
    void downloadSettlementItemExcel(
        @ParameterObject SettlementItemFilter filter,
        @Parameter(hidden = true) Long sellerId,
        @Parameter(hidden = true) HttpServletResponse response
    ) throws IOException;

}
