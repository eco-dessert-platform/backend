package com.bbangle.bbangle.claim.repository.custom;

import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.claim.domain.QCancelRequest.cancelRequest;
import static com.bbangle.bbangle.order.domain.QOrderItem.orderItem;
import static com.bbangle.bbangle.seller.domain.QSeller.seller;
import static com.bbangle.bbangle.store.domain.QStore.store;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CancelRequestCustomRepositoryImpl implements CancelRequestCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Long countCancelsBySeller(List<Long> cancelIds, Long sellerId) {
        return queryFactory
            .select(cancelRequest.id.count())
            .from(cancelRequest)
            .join(cancelRequest.orderItem, orderItem)
            .join(orderItem.product, product)
            .join(product.store, store)
            .join(seller).on(seller.store.eq(store))
            .where(
                cancelRequest.id.in(cancelIds),
                seller.id.eq(sellerId)
            )
            .fetchOne();
    }
}
