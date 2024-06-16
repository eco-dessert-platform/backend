package com.bbangle.bbangle.board.dao;

import com.bbangle.bbangle.board.domain.Category;
import com.querydsl.core.annotations.QueryProjection;

public record BoardResponseDao(
    Long boardId,
    Long storeId,
    String storeName,
    String thumbnail,
    String title,
    Integer price,
    Category category,
    TagsDao tagsDao
) {

    @QueryProjection
    public BoardResponseDao(
        Long boardId,
        Long storeId,
        String storeName,
        String thumbnail,
        String title,
        Integer price,
        Category category,
        boolean glutenFreeTag,
        boolean highProteinTag,
        boolean sugarFreeTag,
        boolean veganTag,
        boolean ketogenicTag
    ) {
        this(boardId, storeId, storeName, thumbnail, title, price, category,
            new TagsDao(glutenFreeTag, highProteinTag, sugarFreeTag, veganTag, ketogenicTag));
    }

}
