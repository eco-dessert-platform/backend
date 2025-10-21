package com.bbangle.bbangle.board.seller.controller.dto.request;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.BoardDetail;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 상세보기 요청 DTO")
public record BoardDetailRequest_v2(
    @Schema(description = "HTML 형식의 상세페이지 내용", example = "<div>...</div>")
    String content
) {

    public BoardDetail toEntity(Board board) {
        return BoardDetail.builder()
            .board(board)
            .content(content)
            .build();
    }
}
