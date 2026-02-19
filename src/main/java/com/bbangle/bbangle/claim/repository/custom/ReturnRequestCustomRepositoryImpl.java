package com.bbangle.bbangle.claim.repository.custom;

import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.claim.domain.QReturnRequest.returnRequest;
import static com.bbangle.bbangle.order.domain.QOrderItem.orderItem;
import static com.bbangle.bbangle.seller.domain.QSeller.seller;
import static com.bbangle.bbangle.store.domain.QStore.store;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReturnRequestCustomRepositoryImpl implements ReturnRequestCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Long countReturnsBySeller(List<Long> returnIds, Long sellerId) {
        return queryFactory
            .select(returnRequest.id.count())
            .from(returnRequest)
            .join(returnRequest.orderItem, orderItem)
            .join(orderItem.product, product)
            .join(product.store, store)
            .join(seller).on(seller.store.eq(store))
            .where(
                returnRequest.id.in(returnIds),
                seller.id.eq(sellerId)
            )
            .fetchOne();
    }
}
