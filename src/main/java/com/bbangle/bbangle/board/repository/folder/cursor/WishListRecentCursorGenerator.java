package com.bbangle.bbangle.board.repository.folder.cursor;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
public class WishListRecentCursorGenerator implements CursorGenerator{

    private static final QWishListBoard wishListBoard = QWishListBoard.wishListBoard;

    private final JPAQueryFactory queryFactory;
    private final Long cursorId;
    private final Long memberId;

    @Override
    public BooleanBuilder getCursor() {
        BooleanBuilder cursorBuilder = new BooleanBuilder();

        if(cursorId == null){
            return cursorBuilder;
        }

        Long wishListBoardId = Optional.ofNullable(queryFactory.select(wishListBoard.id)
                .from(wishListBoard)
                .where(wishListBoard.memberId.eq(memberId)
                    .and(wishListBoard.boardId.eq(cursorId)))
                .fetchOne())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.WISHLIST_BOARD_NOT_FOUND));

        return cursorBuilder.and(wishListBoard.id.loe(wishListBoardId));
    }

}
