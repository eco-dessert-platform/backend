package com.bbangle.bbangle.board.customer.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class AiLearningStoreDto {

    private Long storeId;
    private Long productBoardId;
    private String title;

    @QueryProjection
    public AiLearningStoreDto(Long storeId, Long productBoardId, String title) {
        this.storeId = storeId;
        this.productBoardId = productBoardId;
        this.title = title;
    }
}
