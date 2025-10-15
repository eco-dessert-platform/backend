package com.bbangle.bbangle.wishlist.repository.sort.strategy;

import com.bbangle.bbangle.board.domain.constant.FolderBoardSortType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import java.util.List;

public interface BoardInFolderSortRepository {

    List<Long> findBoardIds(Long cursorId, Long folderId);

    OrderSpecifier<?>[] getSortOrders();

    BooleanBuilder getCursorCondition(Long cursorId);

    FolderBoardSortType getSortType();
}
