package com.bbangle.bbangle.boardstatistic.repository;

import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardStatisticRepositoryImpl implements BoardStatisticQueryDSLRepository {

    private static final QBoardStatistic boardStatistic = QBoardStatistic.boardStatistic;
    private static final QProduct product = QProduct.product;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findPopularBoardIds(int limit) {
        JPQLQuery<Long> subquery = JPAExpressions.select(boardStatistic.boardId)
            .from(boardStatistic)
            .limit(limit);

        return queryFactory.select(
                boardStatistic.boardId
            )
            .from(boardStatistic)
            .join(product).on(boardStatistic.boardId.eq(product.board.id))
            .where(boardStatistic.boardId.in(subquery).and(product.soldout.eq(false)))
            .orderBy(boardStatistic.basicScore.asc())
            .fetch();
    }
}
