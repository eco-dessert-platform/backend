package com.bbangle.bbangle.common.sort;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.repository.folder.cursor.CursorGenerator;
import com.bbangle.bbangle.board.repository.folder.cursor.LowPriceCursorGenerator;
import com.bbangle.bbangle.board.repository.folder.cursor.PopularCursorGenerator;
import com.bbangle.bbangle.board.repository.folder.cursor.WishListRecentCursorGenerator;
import com.bbangle.bbangle.board.repository.folder.query.LowPriceBoardQueryProvider;
import com.bbangle.bbangle.board.repository.folder.query.PopularBoardQueryProvider;
import com.bbangle.bbangle.board.repository.folder.query.QueryGenerator;
import com.bbangle.bbangle.board.repository.folder.query.WishListRecentBoardQueryProvider;
import com.bbangle.bbangle.ranking.domain.QRanking;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public enum FolderBoardSortType {
    LOW_PRICE(QBoard.board.price::asc),
    POPULAR(QRanking.ranking.popularScore::desc),
    WISHLIST_RECENT(QWishListBoard.wishListBoard.id::desc);

    private final Supplier<OrderSpecifier<?>> setOrder;

    FolderBoardSortType(Supplier<OrderSpecifier<?>> setOrder) {
        this.setOrder = setOrder;
    }

    public OrderSpecifier<?> getOrderSpecifier() {
        return setOrder.get();
    }

}
