package com.bbangle.bbangle.board.repository.folder.cursor;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LowPriceInFolderBoardInFolderCursorGenerator implements BoardInFolderCursorGenerator {

    private static final QBoard board = QBoard.board;
    private static final QWishListBoard wishListBoard = QWishListBoard.wishListBoard;

    private final JPAQueryFactory queryFactory;

    @Override
    public BooleanBuilder getCursor(Long cursorId, Long folderId) {
        BooleanBuilder cursorBuilder = new BooleanBuilder();
        if(cursorId == null){
            return cursorBuilder;
        }

        Long wishListBoardId = Optional.ofNullable(queryFactory.select(wishListBoard.id)
                .from(wishListBoard)
                .where(wishListBoard.boardId.eq(cursorId)
                    .and(wishListBoard.wishlistFolderId.eq(folderId)))
                .fetchOne())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.WISHLIST_BOARD_NOT_FOUND));

        Integer price = queryFactory
            .select(board.price)
            .from(board)
            .where(board.id.eq(cursorId))
            .fetchOne();
        cursorBuilder.and(board.price.goe(price)
            .and(wishListBoard.id.loe(wishListBoardId)));

        return cursorBuilder;
    }

}
