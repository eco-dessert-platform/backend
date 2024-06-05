package com.bbangle.bbangle.board.repository.basic.query;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import java.util.List;

// 정렬정보에 따라 쿼리가 아예 다르게 나가야해서 구분하기 위함
public interface BoardQueryProvider {

    List<BoardResponseDao> findBoards(
        BooleanBuilder filter,
        BooleanBuilder cursorInfo,
        OrderSpecifier<?>[] sortOrder
    );
}
