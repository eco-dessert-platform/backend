package com.bbangle.bbangle.board.repository.sort.strategy;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;
import static com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic.boardStatistic;

import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HighestRatedSortRepository implements BoardSortRepository {

    private static final NumberPath<BigDecimal> CRITERIA_ATTRIBUTE = boardStatistic.boardReviewGrade;
    private final JPAQueryFactory  queryFactory ;

    @Override
    public List<Long> findBoardIds(BooleanBuilder filter, Long cursorId) {
        return queryFactory.select(board.id)
                .distinct()
                .from(product)
                .join(board)
                .on(product.board.id.eq(board.id))
                .join(board.boardStatistic, boardStatistic)
                .where(getCursorCondition(cursorId).and(filter))
                .orderBy(getSortOrders())
                .limit(BOARD_PAGE_SIZE + 1)
                .fetch();
    }

    @Override
    public BooleanBuilder getCursorCondition(Long cursorId) {
        BooleanBuilder cursorBuilder = new BooleanBuilder();
        if (cursorId == null) {
            return cursorBuilder;
        }

        BigDecimal targetScore = Optional.ofNullable(
                queryFactory.select(CRITERIA_ATTRIBUTE)
                        .from(boardStatistic)
                        .join(boardStatistic.board, board)
                        .where(board.id.eq(cursorId))
                        .fetchOne()
        ).orElseThrow(() -> new BbangleException(BbangleErrorCode.RANKING_NOT_FOUND));

        cursorBuilder.and(CRITERIA_ATTRIBUTE.lt(targetScore))
                .or(CRITERIA_ATTRIBUTE.eq(targetScore)
                        .and(board.id.loe(cursorId)));
        return cursorBuilder;
    }

    @Override
    public OrderSpecifier<?>[] getSortOrders() {
        return List.of(CRITERIA_ATTRIBUTE.desc(), board.id.desc()).toArray(new OrderSpecifier[0]);
    }

    @Override
    public SortType getSortType() {
        return SortType.HIGHEST_RATED;
    }
}
