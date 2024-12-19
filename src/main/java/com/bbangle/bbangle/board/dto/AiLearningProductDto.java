package com.bbangle.bbangle.board.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class AiLearningProductDto {
    private Long productBoardId;
    private Long productId;
    private String title;

    @QueryProjection
    public AiLearningProductDto(Long productBoardId, Long productId, String title) {
        this.productBoardId = productBoardId;
        this.productId = productId;
        this.title = title;
    }
}
