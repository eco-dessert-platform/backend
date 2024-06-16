package com.bbangle.bbangle.wishlist.repository.impl;

import static com.bbangle.bbangle.exception.BbangleErrorCode.STORE_NOT_FOUND;

import com.bbangle.bbangle.store.domain.QStore;
import com.bbangle.bbangle.wishlist.domain.QWishListStore;
import com.bbangle.bbangle.wishlist.domain.WishListStore;
import com.bbangle.bbangle.wishlist.dto.QWishListStoreResponseDto;
import com.bbangle.bbangle.wishlist.repository.WishListStoreQueryDSLRepository;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.wishlist.dto.WishListStoreCustomPage;
import com.bbangle.bbangle.wishlist.dto.WishListStoreResponseDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WishListStoreRepositoryImpl implements WishListStoreQueryDSLRepository {
    private final JPAQueryFactory queryFactory;
    private static final QWishListStore wishListStore = QWishListStore.wishListStore;
    private static final QStore store = QStore.store;
    private static final Long PAGE_SIZE = 20L;

    @Override
    public List<WishListStore> findWishListStores(Long memberId) {
        return queryFactory
            .selectFrom(wishListStore)
            .where(wishListStore.member.id.eq(memberId)
                .and(wishListStore.isDeleted.eq(false)))
            .fetch();
    }

    @Override
    public WishListStoreCustomPage<List<WishListStoreResponseDto>> getWishListStoreResponse(Long memberId, Long cursorId) {
        BooleanBuilder cursorCondition = getCursorCondition(cursorId, memberId);
        List<WishListStoreResponseDto> responseDtos =
            queryFactory.select(
                new QWishListStoreResponseDto(
                    store.introduce,
                    store.name.as("storeName"),
                    store.id.as("storeId"),
                    store.profile
                ))
            .from(wishListStore)
            .leftJoin(wishListStore.store, store)
            .where(cursorCondition)
            .limit(PAGE_SIZE + 1)
            .orderBy(wishListStore.modifiedAt.desc(),wishListStore.createdAt.desc())
            .fetch();

        boolean hasNext = checkingHasNext(responseDtos);
        int size = responseDtos.size();
        Long requestCursor = size != 0 ? responseDtos.get(size - 1).getStoreId() : 0L;

        if (hasNext) {
            responseDtos.remove(responseDtos.get(size - 1));
        }

        return WishListStoreCustomPage.from(responseDtos, requestCursor, hasNext);
    }

    @Override
    public Optional<WishListStore> findWishListStore(Long memberId, Long storeId) {
        return Optional.ofNullable(queryFactory
            .selectFrom(wishListStore)
            .where(wishListStore.member.id.eq(memberId)
                .and(wishListStore.store.id.eq(storeId)))
            .fetchOne());
    }

    private static boolean checkingHasNext(List<WishListStoreResponseDto> responseDtos) {
        return responseDtos.size() >= PAGE_SIZE + 1;
    }

    private BooleanBuilder getCursorCondition(Long cursorId, Long memberId) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(wishListStore.member.id.eq(memberId))
            .and(wishListStore.isDeleted.eq(false));
        if (Objects.isNull(cursorId)) {
            return booleanBuilder;
        }
        Long startId = checkingStoreExistence(cursorId);

        booleanBuilder.and(store.id.loe(startId));
        return booleanBuilder;
    }

    private Long checkingStoreExistence(Long cursorId) {
        Long checkingId = queryFactory.select(store.id)
            .from(store)
            .where(store.id.eq(cursorId))
            .fetchOne();
        if (Objects.isNull(checkingId)) {
            throw new BbangleException(STORE_NOT_FOUND);
        }
        return cursorId;
    }
}