package com.bbangle.bbangle.vat.seller.excel.controller;

import com.bbangle.bbangle.vat.seller.controller.dto.request.SellerVatSearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Seller Vat Excel", description = "판매자 부가세 신고 내역 엑셀 API")
public interface SellerVatExcelApi {

    @Operation(
        summary = "판매자 부가세 신고 내역 엑셀 다운로드",
        description = "MONTHLY, DAILY, ORDER 형식의 부가세 신고 내역 엑셀 파일을 다운로드합니다."
    )
    void downloadSellerVatExcel(
        @ParameterObject SellerVatSearchRequest request,
        @Parameter(description = "엑셀 다운로드 유형(MONTHLY, DAILY, ORDER)", example = "MONTHLY") String type,
        @Parameter(hidden = true) Long sellerId,
        @Parameter(hidden = true) HttpServletResponse response
    ) throws IOException;
}
