package com.bbangle.bbangle.search.repository.basic.query;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import java.util.List;

public interface SearchQueryProvider {

    List<BoardResponseDao> findBoards(
        List<Long> searchedIds,
        BooleanBuilder filter,
        BooleanBuilder cursorInfo,
        OrderSpecifier<?>[] sortOrder
    );

    Long getCount(
        List<Long> searchedIds,
        BooleanBuilder filter
    );

}
