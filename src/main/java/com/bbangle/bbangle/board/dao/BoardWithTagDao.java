package com.bbangle.bbangle.board.dao;

import com.querydsl.core.annotations.QueryProjection;

public record BoardWithTagDao(
    Long boardId,
    TagsDao tagsDao
) {

    @QueryProjection
    public BoardWithTagDao(
        Long boardId,
        boolean glutenFreeTag,
        boolean highProteinTag,
        boolean sugarFreeTag,
        boolean veganTag,
        boolean ketogenicTag
    ) {
        this(boardId,
            new TagsDao(glutenFreeTag,
                highProteinTag,
                sugarFreeTag,
                veganTag,
                ketogenicTag));
    }

}
