package com.bbangle.bbangle.board.repository.basic.query;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import java.util.List;

public interface BoardQueryProvider {

    List<BoardResponseDao> findBoards(
        BooleanBuilder filter,
        BooleanBuilder cursorInfo,
        OrderSpecifier<?>[] sortOrder
    );

}
