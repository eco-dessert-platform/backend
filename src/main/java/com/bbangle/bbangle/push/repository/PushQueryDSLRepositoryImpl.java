package com.bbangle.bbangle.push.repository;

import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.dto.PushRequest;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.bbangle.bbangle.push.domain.QPush.push;

@Repository
@RequiredArgsConstructor
public class PushQueryDSLRepositoryImpl implements PushQueryDSLRepository {

    private final JPAQueryFactory queryFactory;


    @Override
    public Push findPush(PushRequest request, Long memberId) {
        return queryFactory.selectFrom(push)
                .where(
                        push.memberId.eq(memberId)
                        .and(push.productId.eq(request.productId()))
                        .and(push.pushCategory.eq(PushCategory.valueOf(request.pushCategory())))
                )
                .fetchFirst();
    }

    @Override
    public List<Push> findPushList(PushRequest request, Long memberId) {
        return queryFactory.selectFrom(push)
                .where(
                        push.memberId.eq(memberId)
                        .and(push.pushCategory.eq(PushCategory.valueOf(request.pushCategory())))
                )
                .fetch();
    }
}
