package com.bbangle.bbangle.board.repository.basic.cursor;

import com.bbangle.bbangle.board.repository.folder.cursor.CursorGenerator;
import com.bbangle.bbangle.board.repository.folder.cursor.LowPriceInFolderCursorGenerator;
import com.bbangle.bbangle.board.repository.folder.cursor.PopularCursorGenerator;
import com.bbangle.bbangle.board.repository.folder.cursor.WishListRecentCursorGenerator;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.sort.SortType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BoardCursorGeneratorMapping {

    private final SortType sortType;

    public CursorGenerator mappingCursorGenerator(Long cursorId, JPAQueryFactory jpaQueryFactory) {
        if (sortType == null) {
            return new RecommendCursorGenerator(jpaQueryFactory, cursorId);
        }

        if (sortType == SortType.LOW_PRICE) {
            return new LowPriceCursorGenerator(jpaQueryFactory, cursorId);
        }

        if (sortType == SortType.HIGH_PRICE) {
            return new HighPriceCursorGenerator(jpaQueryFactory, cursorId);
        }

        if (sortType == SortType.RECENT) {
            return new RecentCursorGenerator(jpaQueryFactory, cursorId);
        }

        if (sortType == SortType.HIGHEST_RATED) {
            return new HighRatedCursorGenerator(jpaQueryFactory, cursorId);
        }

        if (sortType == SortType.MOST_WISHED) {
            return new MostWishedCursorGenerator(jpaQueryFactory, cursorId);
        }

        if(sortType == SortType.MOST_REVIEWED) {
            return new MostReviewedCursorGenerator(jpaQueryFactory, cursorId);
        }

        return new RecommendCursorGenerator(jpaQueryFactory, cursorId);
    }

}
