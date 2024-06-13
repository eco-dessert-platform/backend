package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.dao.TagsDao;
import com.bbangle.bbangle.board.domain.Category;
import lombok.Getter;

@Getter
public class TagCategoryDto {

    private Long boardId;
    private Category category;
    private TagsDao tags;

    public TagCategoryDto(
        Long boardId,
        Boolean glutenFreeTag,
        Boolean sugarFreeTag,
        Boolean highProteinTag,
        Boolean veganTag,
        Boolean ketogenicTag,
        Category category
    ) {
        this.boardId = boardId;
        this.category = category;
        this.tags = TagsDao.builder()
            .glutenFreeTag(glutenFreeTag)
            .sugarFreeTag(sugarFreeTag)
            .highProteinTag(highProteinTag)
            .veganTag(veganTag)
            .ketogenicTag(ketogenicTag)
            .build();
    }
}
