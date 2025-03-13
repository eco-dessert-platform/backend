package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.BoardDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BoardDetailRequest {

    @Schema(description = "HTML 형식의 상세페이지 내용")
    private String content; // HTML 형식의 상세페이지 내용

    public BoardDetail toEntity(Board board) {
        return BoardDetail.builder()
                .board(board)
                .content(content)
                .build();
    }
}
