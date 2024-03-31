package com.bbangle.bbangle.wishListStore.repository;

import com.bbangle.bbangle.wishListStore.domain.WishlistStore;
import com.bbangle.bbangle.wishListStore.dto.QWishListStoreResponseDto;
import com.bbangle.bbangle.wishListStore.dto.WishListStoreResponseDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.bbangle.bbangle.store.domain.QStore.store;
import static com.bbangle.bbangle.wishListStore.domain.QWishlistStore.wishlistStore;

@Repository
@RequiredArgsConstructor
public class WishListStoreRepositoryImpl implements WishListStoreQueryDSLRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<WishlistStore> findWishListStores(Long memberId) {
        return queryFactory
                .selectFrom(wishlistStore)
                .where(eqWishStoreMemberId(memberId),
                        isDeletedStore(false))
                .fetch();
    }

    @Override
    public Page<WishListStoreResponseDto> getWishListStoreRes(Long memberId, Pageable pageable) {
        List<WishListStoreResponseDto> wishListStores = queryFactory
                .select(new QWishListStoreResponseDto(
                        wishlistStore.id,
                        store.introduce,
                        store.name.as("storeName"),
                        wishlistStore.store.id.as("storeId")
                ))
                .from(wishlistStore)
                .leftJoin(wishlistStore.store, store)
                .where(eqWishStoreMemberId(memberId),
                        isDeletedStore(false))
                .orderBy(wishlistStore.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<WishListStoreResponseDto> countQuery = queryFactory
                .select(new QWishListStoreResponseDto(
                        wishlistStore.id,
                        store.introduce,
                        store.name.as("storeName"),
                        wishlistStore.store.id.as("storeId")
                ))
                .from(wishlistStore)
                .leftJoin(wishlistStore.store, store)
                .where(eqWishStoreStoreId(memberId),
                        isDeletedStore(false))
                .orderBy(wishlistStore.createdAt.desc());

        return PageableExecutionUtils.getPage(wishListStores, pageable, countQuery::fetchCount);
    }

    @Override
    public Optional<WishlistStore> findWishListStore(Long memberId, Long storeId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(wishlistStore)
                .where(eqWishStoreMemberId(memberId),
                       eqWishStoreStoreId(storeId))
                .fetchOne());
    }

    private BooleanExpression eqWishStoreMemberId(Long memberId){
        return memberId != null ? wishlistStore.member.id.eq(memberId) : null;
    }

    private BooleanExpression eqWishStoreStoreId(Long storeId){
        return storeId != null? wishlistStore.store.id.eq(storeId) : null;
    }

    private BooleanExpression isDeletedStore(boolean validate){
        return wishlistStore.isDeleted.eq(validate);
    }

    @Override
    public List<WishListStoreResponseDto> getWishListStoreResByCursor(Long memberId, Long cursorId, int size) {
        List<WishListStoreResponseDto> wishListStoreResponseDtos = queryFactory
                .select(new QWishListStoreResponseDto(
                        wishlistStore.id,
                        store.introduce,
                        store.name.as("storeName"),
                        wishlistStore.store.id.as("storeId")
                ))
                .from(wishlistStore)
                .leftJoin(wishlistStore.store, store)
                .where(
                        eqWishStoreMemberId(memberId),
                        isDeletedStore(false),
                        wishlistStore.id.loe(cursorId)
                )
                .orderBy(wishlistStore.createdAt.desc())
                .limit(size)
                .fetch();
        return wishListStoreResponseDtos;
    }
}
