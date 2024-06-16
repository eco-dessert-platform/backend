package com.bbangle.bbangle.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "cursor에 필요한 파라미터")
public record CursorInfo(
    @Schema(description = "요청 cursorId", nullable = true, type = "long")
    Long targetId,
    @Schema(description = "요청 score", nullable = true, type = "double")
    Double targetScore
) {

    public boolean isEmpty() {
        return targetId == null || targetScore == null;
    }
}
