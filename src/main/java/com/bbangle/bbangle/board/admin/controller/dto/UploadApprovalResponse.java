package com.bbangle.bbangle.board.admin.controller.dto;

import com.bbangle.bbangle.board.domain.Board;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "업로드 승인 대기 상품 조회 응답")
public record UploadApprovalResponse(
    @Schema(description = "상품(게시글) ID", example = "101")
    Long boardId,

    @Schema(description = "스토어명", example = "빠진 빵집")
    String storeName,

    @Schema(description = "상품명", example = "촉촉한 허니버터 식빵 3종 세트")
    String boardTitle
) {

  public static UploadApprovalResponse fromEntity(Board board) {
    return new UploadApprovalResponse(
        board.getId(),
        board.getStore().getName(),
        board.getTitle()
    );
  }
}
