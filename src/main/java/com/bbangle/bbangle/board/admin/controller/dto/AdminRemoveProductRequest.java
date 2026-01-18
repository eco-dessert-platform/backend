package com.bbangle.bbangle.board.admin.controller.dto;

import static com.bbangle.bbangle.exception.BbangleErrorCode.INVALID_REMOVE_PRODUCTS_REQUEST;

import com.bbangle.bbangle.exception.BbangleException;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "관리자 옵션 제거 요청")
public record AdminRemoveProductRequest(
    @Schema(description = "전체 삭제 여부, false 일 경우 optionIds 필수")
    boolean removeAll,

    @Schema(description = "삭제 대상 옵션 ID List, removeAll이 false 일 경우 필수")
    List<Long> optionIds
) {
    public AdminRemoveProductRequest {
        if (!removeAll && optionIds.isEmpty()) {
            throw new BbangleException(INVALID_REMOVE_PRODUCTS_REQUEST);
        }
    }
}
