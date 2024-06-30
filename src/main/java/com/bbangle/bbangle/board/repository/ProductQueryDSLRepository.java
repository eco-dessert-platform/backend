package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.dto.TagCategoryDto;
import com.bbangle.bbangle.board.dto.TitleDto;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProductQueryDSLRepository {

    Map<Long, Set<Category>> getCategoryInfoByBoardId(List<Long> boardIds);

    List<TagCategoryDto> getTagCategory(List<Long> boardIds);

    List<TitleDto> findAllTitle();
}
