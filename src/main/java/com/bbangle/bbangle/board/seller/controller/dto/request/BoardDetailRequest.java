package com.bbangle.bbangle.board.seller.controller.dto.request;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.BoardDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상세페이지 HTML 정보")
public class BoardDetailRequest {

    @Schema(
        description = "HTML 형식의 상세페이지 내용. 이미지 삽입 시 <img data-id=\"UUID파일명\"> 형태로 포함",
        example = "<div><h2>상품 소개</h2><p>글루텐프리 빵 세트입니다.</p><img data-id=\"img-uuid-001.png\" src=\"blob:temp\"></div>",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    private String content;

    public BoardDetail toEntity(Board board) {
        return BoardDetail.builder()
            .board(board)
            .content(content)
            .build();
    }
}
