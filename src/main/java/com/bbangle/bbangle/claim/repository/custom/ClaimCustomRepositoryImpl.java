package com.bbangle.bbangle.claim.repository.custom;

import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.claim.domain.QClaim.claim;
import static com.bbangle.bbangle.order.domain.QOrderItem.orderItem;
import static com.bbangle.bbangle.seller.domain.QSeller.seller;
import static com.bbangle.bbangle.store.domain.QStore.store;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClaimCustomRepositoryImpl implements ClaimCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsReturnRequestBySeller(Long claimId, Long sellerId) {
        Integer result = queryFactory
            .selectOne()
            .from(claim)
            .join(claim.orderItem, orderItem)
            .join(orderItem.product, product)
            .join(product.store, store)
            .join(seller).on(seller.store.eq(store))
            .where(
                claim.id.eq(claimId),
                seller.id.eq(sellerId)
            )
            .fetchFirst();

        return result != null;
    }
}
