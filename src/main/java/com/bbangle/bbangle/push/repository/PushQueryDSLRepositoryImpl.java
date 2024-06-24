package com.bbangle.bbangle.push.repository;

import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.dto.PushResponse;
import com.bbangle.bbangle.push.dto.QPushResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.push.domain.QPush.push;
import static com.bbangle.bbangle.store.domain.QStore.store;

@Repository
@RequiredArgsConstructor
public class PushQueryDSLRepositoryImpl implements PushQueryDSLRepository {

    private final JPAQueryFactory queryFactory;


    @Override
    public Push findPush(Long productId, String pushCategory, Long memberId) {
        return queryFactory.selectFrom(push)
                .where(commonFilter(memberId, productId, pushCategory))
                .fetchFirst();
    }

    @Override
    public List<PushResponse> findPushList(String pushCategory, Long memberId) {
        return queryFactory.select(new QPushResponse(
                        product.id,
                        store.name,
                        product.title,
                        board.profile,
                        push.subscribed
                ))
                .from(push)
                .join(product).on(push.productId.eq(product.id))
                .join(board).on(product.board.id.eq(board.id))
                .join(store).on(board.store.id.eq(store.id))
                .where(commonFilter(memberId, null, pushCategory))
                .fetch();
    }


    private BooleanBuilder commonFilter(Long memberId, Long productId, String pushCategory) {
        BooleanBuilder builder = new BooleanBuilder();

        if (memberId != null) {
            builder.and(push.memberId.eq(memberId));
        }
        if (productId != null) {
            builder.and(push.productId.eq(productId));
        }
        if (pushCategory != null) {
            builder.and(push.pushCategory.eq(PushCategory.valueOf(pushCategory)));
        }

        return builder;
    }

}
