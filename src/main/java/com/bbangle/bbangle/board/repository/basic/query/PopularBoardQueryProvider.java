package com.bbangle.bbangle.board.repository.query;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.ranking.domain.QRanking;
import com.bbangle.bbangle.store.domain.QStore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PopularBoardQueryProvider implements BoardQueryProvider {

    private final JPAQueryFactory queryFactory;
    private final Long cursorId;
    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;
    private static final QStore store = QStore.store;
    private static final QRanking ranking = QRanking.ranking;

    @Override
    public List<Board> findBoards(BooleanBuilder filter) {
        BooleanBuilder cursorBuilder = getCursorBuilder();

        List<Long> fetch = queryFactory
            .select(board.id)
            .distinct()
            .from(product)
            .join(product.board, board)
            .join(ranking)
            .on(board.id.eq(ranking.board.id))
            .where(cursorBuilder.and(filter))
            .orderBy(ranking.popularScore.desc(), board.id.desc())
            .limit(BOARD_PAGE_SIZE + 1L)
            .fetch();

        return queryFactory.select(board)
            .from(board)
            .join(board.productList, product)
            .fetchJoin()
            .join(board.store, store)
            .fetchJoin()
            .join(ranking)
            .on(board.id.eq(ranking.board.id))
            .where(board.id.in(fetch))
            .orderBy(ranking.popularScore.desc(), board.id.desc())
            .fetch();
    }

    private BooleanBuilder getCursorBuilder() {
        BooleanBuilder cursorBuilder = new BooleanBuilder();
        if (cursorId == null) {
            return cursorBuilder;
        }

        Double targetScore = Optional.ofNullable(queryFactory.select(ranking.popularScore)
                .from(ranking)
                .where(ranking.board.id.eq(cursorId)))
            .orElseThrow(() -> new BbangleException(
                BbangleErrorCode.RANKING_NOT_FOUND))
            .fetchOne();

        cursorBuilder.and(ranking.popularScore.lt(targetScore))
            .or(ranking.popularScore.eq(targetScore)
                .and(board.id.lt(cursorId)));
        return cursorBuilder;
    }

}
