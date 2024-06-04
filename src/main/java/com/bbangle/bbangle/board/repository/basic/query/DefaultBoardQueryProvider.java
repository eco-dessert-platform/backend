package com.bbangle.bbangle.board.repository.query;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.dto.CursorInfo;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.ranking.domain.QRanking;
import com.bbangle.bbangle.store.domain.QStore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultBoardQueryProvider implements BoardQueryProvider {

    private final JPAQueryFactory queryFactory;
    private final SortType sortType;
    private final CursorInfo cursorInfo;
    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;
    private static final QStore store = QStore.store;
    private static final QRanking ranking = QRanking.ranking;

    @Override
    public List<Board> findBoards(BooleanBuilder filter) {
        OrderSpecifier<Double> orderExpression = getOrderExpression(sortType);
        BooleanBuilder cursorBuilder = getCursorBuilder();

        List<Long> fetch = queryFactory
            .select(board.id)
            .distinct()
            .from(product)
            .join(product.board, board)
            .join(ranking)
            .on(board.id.eq(ranking.board.id))
            .where(cursorBuilder.and(filter))
            .orderBy(orderExpression, board.id.desc())
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
            .orderBy(orderExpression, board.id.desc())
            .fetch();
    }

    private OrderSpecifier<Double> getOrderExpression(SortType sort) {
        if (Objects.isNull(sort) || sort.equals(SortType.RECOMMEND)) {
            return ranking.recommendScore.desc();
        }

        return ranking.popularScore.desc();
    }

    private BooleanBuilder getCursorBuilder() {
        BooleanBuilder cursorBuilder = new BooleanBuilder();
        if (cursorInfo == null || cursorInfo.isEmpty() || sortType == null) {
            return cursorBuilder;
        }

        cursorBuilder.and(ranking.recommendScore.loe(cursorInfo.targetScore()))
            .and(board.id.lt(cursorInfo.targetId()));
        return cursorBuilder;
    }
}
