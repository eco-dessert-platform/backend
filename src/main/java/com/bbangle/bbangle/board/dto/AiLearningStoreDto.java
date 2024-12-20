package com.bbangle.bbangle.board.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class AiLearningStoreDto {
    private Long productBoardId;
    private Long storeId;
    private String title;

    @QueryProjection
    public AiLearningStoreDto(Long productBoardId, Long storeId, String title) {
        this.productBoardId = productBoardId;
        this.storeId = storeId;
        this.title = title;
    }
}
