package com.bbangle.bbangle.boardstatistic.repository;

import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
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
        List<Long> boardIds = queryFactory.select(boardStatistic.board.id)
            .from(boardStatistic)
            .orderBy(boardStatistic.basicScore.desc())
            .limit(limit)
            .fetch();

        return queryFactory.select(
                boardStatistic.board.id
            )
            .from(boardStatistic)
            .join(product).on(boardStatistic.board.id.eq(product.board.id))
            .where(boardStatistic.board.id.in(boardIds).and(product.soldout.eq(false)))
            .orderBy(boardStatistic.basicScore.asc())
            .fetch()
            .stream()
            .distinct()
            .toList();
    }
}
