package com.bbangle.bbangle.board.sort;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.ranking.domain.QRanking;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.querydsl.core.types.OrderSpecifier;
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
