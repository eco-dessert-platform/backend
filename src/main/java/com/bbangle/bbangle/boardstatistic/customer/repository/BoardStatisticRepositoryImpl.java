package com.bbangle.bbangle.boardstatistic.customer.repository;

import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.bbangle.bbangle.board.domain.QBoard.board;

@Repository
@RequiredArgsConstructor
public class BoardStatisticRepositoryImpl implements BoardStatisticQueryDSLRepository {

    private static final QBoardStatistic boardStatistic = QBoardStatistic.boardStatistic;
    private static final QProduct product = QProduct.product;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findPopularBoardIds(int limit) {
        List<Long> boardIds = queryFactory.select(board.id)
            .from(boardStatistic)
            .join(boardStatistic.board, board)
            .orderBy(boardStatistic.basicScore.desc())
            .limit(limit)
            .fetch();

        return queryFactory.select(
                board.id
            )
            .from(boardStatistic)
            .join(boardStatistic.board, board)
            .join(board.products, product)
            .where(
                board.id.in(boardIds),
                product.soldout.eq(false))
            .orderBy(boardStatistic.basicScore.asc())
            .fetch()
            .stream()
            .distinct()
            .toList();
    }
}
