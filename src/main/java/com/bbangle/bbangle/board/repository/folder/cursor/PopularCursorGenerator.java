package com.bbangle.bbangle.board.repository.folder.cursor;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.ranking.domain.QRanking;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PopularCursorGenerator implements CursorGenerator{

    private static final QRanking ranking = QRanking.ranking;
    private static final QBoard board = QBoard.board;
    private static final QWishListBoard wishListBoard = QWishListBoard.wishListBoard;

    private final JPAQueryFactory queryFactory;
    private final Long cursorId;
    private final Long folderId;

    @Override
    public BooleanBuilder getCursor() {
        BooleanBuilder cursorBuilder = new BooleanBuilder();
        if(cursorId == null){
            return cursorBuilder;
        }

        Optional.ofNullable(queryFactory.select(wishListBoard.id)
                .from(wishListBoard)
                .where(wishListBoard.boardId.eq(cursorId).and(wishListBoard.wishlistFolderId.eq(folderId)))
                .fetchOne())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.WISHLIST_BOARD_NOT_FOUND));

        Double score = queryFactory
            .select(ranking.popularScore)
            .from(ranking)
            .where(ranking.board.id.eq(cursorId))
            .fetchOne();
        cursorBuilder.and(ranking.popularScore.loe(score)
            .and(wishListBoard.id.loe(cursorId)));

        return cursorBuilder;
    }

}
