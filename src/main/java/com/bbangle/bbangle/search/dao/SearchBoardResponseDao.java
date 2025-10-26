package com.bbangle.bbangle.search.dao;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.repository.dao.TagsDao;
import com.querydsl.core.annotations.QueryProjection;

public record SearchBoardResponseDao(
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
    public SearchBoardResponseDao(
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
