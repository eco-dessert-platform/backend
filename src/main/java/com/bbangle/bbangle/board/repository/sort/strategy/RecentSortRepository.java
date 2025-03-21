package com.bbangle.bbangle.board.repository.sort.strategy;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.sort.SortType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecentSortRepository implements BoardSortRepository {

    private static final NumberPath<Long> CRITERIA_ATTRIBUTE = board.id;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findBoardIds(BooleanBuilder filter, Long cursorId) {
        return queryFactory.select(board.id)
                .distinct()
                .from(product)
                .join(board)
                .on(product.board.id.eq(board.id))
                .where(getCursorCondition(cursorId).and(filter))
                .orderBy(getSortOrders())
                .limit(BOARD_PAGE_SIZE + 1)
                .fetch();
    }

    @Override
    public BooleanBuilder getCursorCondition(Long cursorId) {
        BooleanBuilder cursorBuilder = new BooleanBuilder();
        if (cursorId != null) {
            cursorBuilder.and(CRITERIA_ATTRIBUTE.loe(cursorId));
        }
        return cursorBuilder;
    }

    @Override
    public OrderSpecifier<?>[] getSortOrders() {
        return List.of(CRITERIA_ATTRIBUTE.desc()).toArray(new OrderSpecifier[0]);
    }

    @Override
    public SortType getSortType() {
        return SortType.RECENT;
    }
}
