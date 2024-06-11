package com.bbangle.bbangle.board.repository.folder.query;

import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
public class BoardInFolderQueryGeneratorMapping {

    private final BooleanBuilder cursorBuilder;
    private final JPAQueryFactory jpaQueryFactory;
    private final OrderSpecifier<?> order;
    private final WishListFolder wishListFolder;
    private final FolderBoardSortType sortType;

    public QueryGenerator mappingQueryGenerator() {
        if(sortType == null){
            return new WishListRecentBoardQueryProvider(jpaQueryFactory, cursorBuilder, order, wishListFolder);
        }

        if (sortType == FolderBoardSortType.LOW_PRICE) {
            return new LowPriceBoardQueryProvider(jpaQueryFactory, cursorBuilder, order, wishListFolder);
        }

        if (sortType == FolderBoardSortType.POPULAR) {
            return new PopularBoardQueryProvider(jpaQueryFactory, cursorBuilder, order, wishListFolder);
        }

        return new WishListRecentBoardQueryProvider(jpaQueryFactory, cursorBuilder, order, wishListFolder);
    }
}
