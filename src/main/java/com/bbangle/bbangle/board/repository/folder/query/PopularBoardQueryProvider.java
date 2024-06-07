package com.bbangle.bbangle.board.repository.folder.query;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dao.QBoardResponseDao;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.ranking.domain.QBoardStatistic;
import com.bbangle.bbangle.store.domain.QStore;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PopularBoardQueryProvider implements QueryGenerator{

    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;
    private static final QStore store = QStore.store;
    private static final QBoardStatistic boardStatistic = QBoardStatistic.boardStatistic;
    private static final QWishListBoard wishListBoard = QWishListBoard.wishListBoard;

    private final JPAQueryFactory queryFactory;
    private final BooleanBuilder cursorBuilder;
    private final OrderSpecifier<?> order;
    private final WishListFolder folder;

    @Override
    public List<BoardResponseDao> getBoards() {
        List<Long> fetch = queryFactory
            .select(board.id)
            .from(board)
            .join(wishListBoard)
            .on(board.id.eq(wishListBoard.boardId))
            .join(boardStatistic)
            .on(board.id.eq(boardStatistic.boardId))
            .where(wishListBoard.wishlistFolderId.eq(folder.getId())
                .and(cursorBuilder))
            .orderBy(order, wishListBoard.id.desc())
            .limit(BOARD_PAGE_SIZE + 1L)
            .fetch();

        return queryFactory.select(
                new QBoardResponseDao(
                    board.id,
                    store.id,
                    store.name,
                    store.profile,
                    board.title,
                    board.price,
                    product.category,
                    product.glutenFreeTag,
                    product.highProteinTag,
                    product.sugarFreeTag,
                    product.veganTag,
                    product.ketogenicTag
                )).from(product)
            .join(board)
            .on(product.board.id.eq(board.id))
            .join(store)
            .on(board.store.id.eq(store.id))
            .join(wishListBoard)
            .on(board.id.eq(wishListBoard.boardId))
            .join(boardStatistic)
            .on(board.id.eq(boardStatistic.boardId))
            .where(board.id.in(fetch).and(wishListBoard.wishlistFolderId.eq(folder.getId())))
            .orderBy(order, wishListBoard.id.desc())
            .fetch();
    }

}
