package com.bbangle.bbangle.board.repository.folder.cursor;

import com.bbangle.bbangle.common.sort.FolderBoardSortType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BoardInFolderCursorGeneratorMapping {

    private final Long memberId;
    private final Long cursorId;
    private final JPAQueryFactory jpaQueryFactory;
    private final FolderBoardSortType sortType;

    public CursorGenerator mappingCursorGenerator() {
        if(sortType == null){
            return new WishListRecentCursorGenerator(jpaQueryFactory, cursorId, memberId);
        }

        if (sortType == FolderBoardSortType.LOW_PRICE) {
            return new LowPriceCursorGenerator(jpaQueryFactory, cursorId, memberId);
        }

        if (sortType == FolderBoardSortType.POPULAR) {
            return new PopularCursorGenerator(jpaQueryFactory, cursorId, memberId);
        }

        return new WishListRecentCursorGenerator(jpaQueryFactory, cursorId, memberId);
    }

}
