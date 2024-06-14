package com.bbangle.bbangle.board.repository.folder.cursor;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PopularBoardInFolderCursorGenerator implements BoardInFolderCursorGenerator {

    private static final QBoardStatistic boardStatistic = QBoardStatistic.boardStatistic;
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

        Double score = queryFactory
            .select(boardStatistic.basicScore)
            .from(boardStatistic)
            .where(boardStatistic.boardId.eq(cursorId))
            .fetchOne();
        cursorBuilder.and(boardStatistic.basicScore.loe(score)
            .and(wishListBoard.id.loe(wishListBoardId)));

        return cursorBuilder;
    }

}
