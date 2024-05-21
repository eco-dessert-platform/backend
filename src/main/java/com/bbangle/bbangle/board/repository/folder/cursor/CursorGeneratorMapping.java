package com.bbangle.bbangle.board.repository.folder.cursor;

import com.bbangle.bbangle.common.sort.FolderBoardSortType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CursorGeneratorMapping {

    private final Long memberId;
    private final JPAQueryFactory jpaQueryFactory;
    private final FolderBoardSortType sortType;

    public CursorGenerator mappingCursorGenerator() {
        if (sortType == FolderBoardSortType.LOW_PRICE) {
            return new LowPriceCursorGenerator(jpaQueryFactory);
        }

        if (sortType == FolderBoardSortType.POPULAR) {
            return new PopularCursorGenerator(jpaQueryFactory);
        }

        return new WishListRecentCursorGenerator(jpaQueryFactory, memberId);
    }

}
