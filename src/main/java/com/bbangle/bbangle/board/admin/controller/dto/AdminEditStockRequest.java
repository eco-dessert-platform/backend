package com.bbangle.bbangle.board.admin.controller.dto;

import static com.bbangle.bbangle.exception.BbangleErrorCode.INVALID_STOCK_AMOUNT;

import com.bbangle.bbangle.board.domain.EditStockFlag;
import com.bbangle.bbangle.exception.BbangleException;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 재고 변경 요청")
public record AdminEditStockRequest(
    @Schema(description = "재고 조작 Flag")
    EditStockFlag editStockFlag,
    @Schema(description = "조작 수량")
    int amount
) {
    public AdminEditStockRequest {
        if (amount < 0) {
            throw new BbangleException(INVALID_STOCK_AMOUNT);
        }
    }
}
