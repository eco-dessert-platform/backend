package com.bbangle.bbangle.board.customer.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class AiLearningReviewDto {

    private Long productBoardId;
    private String content;

    @QueryProjection
    public AiLearningReviewDto(Long productBoardId, String content) {
        this.productBoardId = productBoardId;
        this.content = content;
    }
}
