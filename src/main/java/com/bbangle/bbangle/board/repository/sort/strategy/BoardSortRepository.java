package com.bbangle.bbangle.board.repository.sort.strategy;

import com.bbangle.bbangle.board.sort.SortType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import java.util.List;

public interface BoardSortRepository {

    List<Long> findBoardIds(BooleanBuilder filter, Long cursorId);

    OrderSpecifier<?>[] getSortOrders();

    BooleanBuilder getCursorCondition(Long cursorId);

    SortType getSortType();
}
