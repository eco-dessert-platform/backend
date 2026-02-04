package com.bbangle.bbangle.board.admin.controller.swagger;

import com.bbangle.bbangle.board.admin.controller.dto.AdminEditStockRequest;
import com.bbangle.bbangle.common.dto.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "AdminOptions", description = "(관리자) 상품 옵션 관련 API")
public interface AdminProductApi {

    @Operation(
        summary = "관리자 상품옵션 재고 수정",
        description = "관리자가 선택한 상품옵션의 재고를 수정합니다."
    )
    CommonResult editProductOptionStock(
        @Parameter(description = "재고를 수정할 상품 옵션ID", example = "1")
        Long optionId,
        @RequestBody @Valid AdminEditStockRequest adminEditStockRequest
    );
}
