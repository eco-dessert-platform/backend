package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.store.dto.QStoreDetailStoreDto;
import com.bbangle.bbangle.store.dto.QStoreDto;
import com.bbangle.bbangle.store.domain.QStore;
import com.bbangle.bbangle.store.dto.StoreDetailStoreDto;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.wishlist.domain.QWishListStore;
import com.bbangle.bbangle.wishlist.repository.util.WishListStoreFilter;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreQueryDSLRepository {

    private static final QStore store = QStore.store;
    private static final QBoard board = QBoard.board;
    private static final  QWishListStore wishListStore = QWishListStore.wishListStore;

    private final JPAQueryFactory queryFactory;
    private final WishListStoreFilter wishListStoreFilter;

    @Override
    public StoreDetailStoreDto getStoreResponse(Long memberId, Long storeId) {
        return queryFactory.select(
                new QStoreDetailStoreDto(
                    store.id,
                    store.profile,
                    store.name,
                    store.introduce,
                    wishListStore.id)
            ).from(store)
            .where(store.id.eq(storeId))
            .leftJoin(wishListStore)
            .on(wishListStoreFilter.equalMemberId(memberId)
                .and(wishListStoreFilter.equalStoreId(store))
                .and(wishListStore.isDeleted.isFalse()))
            .fetchOne();
    }

    @Override
    public StoreDto findByBoardId(Long boardId) {
        return queryFactory.select(
                new QStoreDto(
                    store.id,
                    store.name,
                    store.profile
                )
            ).from(board)
            .join(store).on(store.eq(board.store))
            .where(board.id.eq(boardId))
            .fetchFirst();
    }
}
