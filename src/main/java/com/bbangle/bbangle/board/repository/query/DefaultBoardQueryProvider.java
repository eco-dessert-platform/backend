package com.bbangle.bbangle.board.repository.query;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.dto.CursorInfo;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.ranking.domain.QBoardStatistic;
import com.bbangle.bbangle.store.domain.QStore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultBoardQueryProvider implements BoardQueryProvider {

    private final JPAQueryFactory queryFactory;
    private final SortType sortType;
    private final CursorInfo cursorInfo;
    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;
    private static final QStore store = QStore.store;
    private static final QBoardStatistic boardStatistic = QBoardStatistic.boardStatistic;

    @Override
    public List<Board> findBoards(BooleanBuilder filter) {
        BooleanBuilder cursorBuilder = getCursorBuilder();

        List<Long> fetch = queryFactory
            .select(board.id)
            .distinct()
            .from(product)
            .join(product.board, board)
            .join(boardStatistic)
            .on(board.id.eq(boardStatistic.boardId))
            .where(cursorBuilder.and(filter))
            .orderBy(boardStatistic.basicScore.desc(), board.id.desc())
            .limit(BOARD_PAGE_SIZE + 1L)
            .fetch();

        return queryFactory.select(board)
            .from(board)
            .join(board.productList, product)
            .fetchJoin()
            .join(board.store, store)
            .fetchJoin()
            .join(boardStatistic)
            .on(board.id.eq(boardStatistic.boardId))
            .where(board.id.in(fetch))
            .orderBy(boardStatistic.basicScore.desc(), board.id.desc())
            .fetch();
    }

    private BooleanBuilder getCursorBuilder() {
        BooleanBuilder cursorBuilder = new BooleanBuilder();
        if (cursorInfo == null || cursorInfo.isEmpty() || sortType == null) {
            return cursorBuilder;
        }

        cursorBuilder.and(boardStatistic.basicScore.loe(cursorInfo.targetScore()))
            .and(board.id.lt(cursorInfo.targetId()));
        return cursorBuilder;
    }
}
