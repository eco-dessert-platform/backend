package com.bbangle.bbangle.board.repository.basic.query;

import static com.bbangle.bbangle.board.domain.QProductImg.productImg;
import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dao.QBoardResponseDao;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.bbangle.bbangle.store.domain.QStore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecentBoardQueryProviderResolver implements BoardQueryProvider {

    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;
    private static final QStore store = QStore.store;
    private static final QBoardStatistic boardStatistic = QBoardStatistic.boardStatistic;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<BoardResponseDao> findBoards(
            BooleanBuilder filter,
            BooleanBuilder cursorInfo,
            OrderSpecifier<?>[] orderCondition
    ) {
        List<Long> boardIds = jpaQueryFactory.select(board.id)
                .distinct()
                .from(product)
                .join(board)
                .on(product.board.id.eq(board.id))
                .where(cursorInfo.and(filter))
                .orderBy(orderCondition)
                .limit(BOARD_PAGE_SIZE + 1)
                .fetch();

        return jpaQueryFactory.select(
                        new QBoardResponseDao(
                                board.id,
                                store.id,
                                store.name,
                                productImg.url,
                                board.title,
                                board.price,
                                product.category,
                                product.glutenFreeTag,
                                product.highProteinTag,
                                product.sugarFreeTag,
                                product.veganTag,
                                product.ketogenicTag,
                                boardStatistic.boardReviewGrade,
                                boardStatistic.boardReviewCount,
                                product.orderEndDate,
                                product.soldout,
                                board.discountRate
                        ))
                .from(product)
                .join(board)
                .on(product.board.id.eq(board.id))
                .join(store)
                .on(board.store.id.eq(store.id))
                .join(board.boardStatistic, boardStatistic)
                .innerJoin(productImg).on(board.id.eq(productImg.board.id).and(productImg.imgOrder.eq(0)))
                .where(board.id.in(boardIds))
                .orderBy(orderCondition)
                .fetch();
    }

}
