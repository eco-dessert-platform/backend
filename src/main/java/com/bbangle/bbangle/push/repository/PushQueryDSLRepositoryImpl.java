package com.bbangle.bbangle.push.repository;

import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.dto.FcmPush;
import com.bbangle.bbangle.push.dto.PushResponse;
import com.bbangle.bbangle.push.dto.QFcmPush;
import com.bbangle.bbangle.push.dto.QPushResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.board.domain.QProductImg.productImg;
import static com.bbangle.bbangle.member.domain.QMember.member;
import static com.bbangle.bbangle.push.domain.QPush.push;
import static com.bbangle.bbangle.store.domain.QStore.store;

@Repository
@RequiredArgsConstructor
public class PushQueryDSLRepositoryImpl implements PushQueryDSLRepository {

    private final JPAQueryFactory queryFactory;


    @Override
    public Push findPush(Long productId, PushCategory pushCategory, Long memberId) {
        return queryFactory.selectFrom(push)
                .where(commonFilter(memberId, productId, pushCategory))
                .fetchFirst();
    }

    @Override
    public List<PushResponse> findPushList(PushCategory pushCategory, Long memberId) {
        return queryFactory.select(new QPushResponse(
                        product.id,
                        store.name,
                        product.title,
                        productImg.url,
                        push.isActive
                ))
                .from(push)
                .join(product).on(push.productId.eq(product.id))
                .join(board).on(product.board.id.eq(board.id))
                .join(store).on(board.store.id.eq(store.id))
                .innerJoin(productImg).on(board.id.eq(productImg.board.id).and(productImg.imgOrder.eq(0)))
                .where(commonFilter(memberId, null, pushCategory))
                .fetch();
    }


    @Override
    public List<FcmPush> findPushList() {
        return queryFactory.select(new QFcmPush(
                        push.id,
                        push.fcmToken,
                        member.nickname,
                        board.title,
                        product.id,
                        product.title,
                        push.pushType,
                        push.days,
                        push.pushCategory,
                        product.soldout,
                        product.orderStartDate
                ))
                .from(push)
                .join(member).on(push.memberId.eq(member.id))
                .join(product).on(push.productId.eq(product.id))
                .join(board).on(product.board.id.eq(board.id))
                .where(push.isActive.isTrue())
                .fetch();
    }

    @Override
    public List<Long> findExistingPushProductIds(List<Long> productIds, Long memberId) {
        return queryFactory.select(push.productId)
            .from(push)
            .where(push.productId.in(productIds),
                push.memberId.eq(memberId))
            .fetch();
    }

    private BooleanBuilder commonFilter(Long memberId, Long productId, PushCategory pushCategory) {
        BooleanBuilder builder = new BooleanBuilder();

        if (memberId != null) {
            builder.and(push.memberId.eq(memberId));
        }
        if (productId != null) {
            builder.and(push.productId.eq(productId));
        }
        if (pushCategory != null) {
            builder.and(push.pushCategory.eq(pushCategory));
        }

        return builder;
    }

}
