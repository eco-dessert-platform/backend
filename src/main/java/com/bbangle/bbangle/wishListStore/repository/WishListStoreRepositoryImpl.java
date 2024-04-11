package com.bbangle.bbangle.wishListStore.repository;

import static com.bbangle.bbangle.store.domain.QStore.store;
import static com.bbangle.bbangle.wishListStore.domain.QWishlistStore.wishlistStore;

import com.bbangle.bbangle.wishListStore.dto.QWishListStoreResponseDto;
import com.bbangle.bbangle.wishListStore.dto.WishListStoreResponseDto;
import com.bbangle.bbangle.wishListStore.domain.WishlistStore;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WishListStoreRepositoryImpl implements WishListStoreQueryDSLRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<WishlistStore> findWishListStores(Long memberId) {
        return queryFactory
                .selectFrom(wishlistStore)
                .where(wishlistStore.member.id.eq(memberId)
                        .and(wishlistStore.isDeleted.eq(false)))
                .fetch();
    }

    @Override
    public Page<WishListStoreResponseDto> getWishListStoreResponse(Long memberId, Pageable pageable) {
        List<WishListStoreResponseDto> wishListStores = queryFactory
                .select(new QWishListStoreResponseDto(
                        wishlistStore.id,
                        store.introduce,
                        store.name.as("storeName"),
                        store.id.as("storeId"),
                        store.profile
                ))
                .from(wishlistStore)
                .leftJoin(wishlistStore.store, store)
                .where(wishlistStore.member.id.eq(memberId).and(wishlistStore.isDeleted.ne(true)))
                .orderBy(wishlistStore.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(wishlistStore.id.count())
                .from(wishlistStore)
                .leftJoin(wishlistStore.store, store)
                .where(wishlistStore.member.id.eq(memberId).and(wishlistStore.isDeleted.ne(true)))
                .orderBy(wishlistStore.createdAt.desc());

        return PageableExecutionUtils.getPage(wishListStores, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<WishlistStore> findWishListStore(Long memberId, Long storeId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(wishlistStore)
                .where(wishlistStore.member.id.eq(memberId)
                        .and(wishlistStore.store.id.eq(storeId)))
                .fetchOne());
    }
}
