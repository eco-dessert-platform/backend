package com.bbangle.bbangle.board.seller.controller.dto.request;

import com.bbangle.bbangle.board.domain.SaleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class SellerBoardRequest {

    /**
     * 판매 상태(SaleStatus) 변경 요청 DTO.
     * 판매자가 게시글의 판매를 명시적으로 중지(STOPPED)하거나 재개(ON_SALE)할 때 사용한다.
     * 그 외의 상태(PENDING, BANNED, OUT_OF_STOCK)로의 요청은 서비스 단에서 거부된다.
     */
    @Schema(description = "판매 상태 변경 요청 DTO")
    public record UpdateSaleStatusRequest(

        @Schema(description = "변경할 판매 상태 (STOPPED: 판매중지, ON_SALE: 판매재개)", example = "STOPPED")
        @NotNull(message = "변경할 판매 상태는 필수입니다.")
        SaleStatus saleStatus
    ) {}
}
