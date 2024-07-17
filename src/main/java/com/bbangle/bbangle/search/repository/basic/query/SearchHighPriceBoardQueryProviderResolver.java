package com.bbangle.bbangle.search.repository.basic.query;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dao.QBoardResponseDao;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.store.domain.QStore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchHighPriceBoardQueryProviderResolver implements SearchQueryProvider {

    private static final Integer BOARD_PAGE_SIZE_PLUS_ONE = BOARD_PAGE_SIZE + 1;
    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;
    private static final QStore store = QStore.store;

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<BoardResponseDao> findBoards(
        List<Long> searchedIds,
        BooleanBuilder filter,
        BooleanBuilder cursorInfo,
        OrderSpecifier<?>[] orderCondition
    ) {
        List<Long> boardIds = jpaQueryFactory.select(board.id)
            .distinct()
            .from(product)
            .join(board)
            .on(product.board.id.eq(board.id))
            .where(cursorInfo.and(filter).and(board.id.in(searchedIds)))
            .orderBy(orderCondition)
            .limit(BOARD_PAGE_SIZE_PLUS_ONE)
            .fetch();

        return jpaQueryFactory.select(
                new QBoardResponseDao(
                    board.id,
                    store.id,
                    store.name,
                    board.profile,
                    board.title,
                    board.price,
                    product.category,
                    product.glutenFreeTag,
                    product.highProteinTag,
                    product.sugarFreeTag,
                    product.veganTag,
                    product.ketogenicTag
                ))
            .from(product)
            .join(board)
            .on(product.board.id.eq(board.id))
            .join(store)
            .on(board.store.id.eq(store.id))
            .where(board.id.in(boardIds))
            .orderBy(orderCondition)
            .fetch();
    }

    @Override
    public Long getCount(
        List<Long> searchedIds,
        BooleanBuilder filter
    ) {
        return jpaQueryFactory.select(board.id)
            .distinct()
            .from(product)
            .join(board)
            .on(product.board.id.eq(board.id))
            .where(filter.and(board.id.in(searchedIds)))
            .fetch().stream().count();
    }
}
