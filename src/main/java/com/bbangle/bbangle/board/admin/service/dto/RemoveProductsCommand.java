package com.bbangle.bbangle.board.admin.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record RemoveProductsCommand(
    @Schema(description = "대상 상품 ID")
    Long boardId,

    @Schema(description = "전체 삭제 여부, false 일 경우 optionIds 필수")
    boolean removeAll,

    @Schema(description = "삭제 대상 옵션 ID List, removeAll이 false 일 경우 필수")
    List<Long> productIds
) {
}
