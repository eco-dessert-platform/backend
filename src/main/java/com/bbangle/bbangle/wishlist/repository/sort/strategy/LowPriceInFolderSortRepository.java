package com.bbangle.bbangle.wishlist.repository.sort.strategy;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;
import static com.bbangle.bbangle.wishlist.domain.QWishListBoard.wishListBoard;

import com.bbangle.bbangle.board.domain.constant.FolderBoardSortType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LowPriceInFolderSortRepository implements BoardInFolderSortRepository {

    private static final NumberPath<Integer> CRITERIA_ATTRIBUTE = board.price;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findBoardIds(Long cursorId, Long folderId) {
        return queryFactory
                .select(board.id)
                .from(board)
                .join(wishListBoard)
                .on(board.id.eq(wishListBoard.boardId)
                        .and(wishListBoard.wishlistFolderId.eq(folderId)))
                .where(getCursorCondition(cursorId))
                .orderBy(getSortOrders())
                .limit(BOARD_PAGE_SIZE + 1L)
                .fetch();
    }

    @Override
    public BooleanBuilder getCursorCondition(Long cursorId) {
        BooleanBuilder cursorBuilder = new BooleanBuilder();
        if (cursorId == null) {
            return cursorBuilder;
        }

        Integer price = queryFactory
                .select(board.price)
                .from(board)
                .where(board.id.eq(cursorId))
                .fetchOne();
        cursorBuilder.and(board.price.goe(price)
                .and(board.id.loe(cursorId)));

        return cursorBuilder;
    }

    @Override
    public OrderSpecifier<?>[] getSortOrders() {
        return List.of(CRITERIA_ATTRIBUTE.asc(), board.id.desc()).toArray(new OrderSpecifier[0]);
    }

    @Override
    public FolderBoardSortType getSortType() {
        return FolderBoardSortType.LOW_PRICE;
    }
}
