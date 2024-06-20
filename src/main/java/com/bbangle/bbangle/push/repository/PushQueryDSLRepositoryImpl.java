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
import static com.bbangle.bbangle.push.domain.QPush.push;
import static com.bbangle.bbangle.store.domain.QStore.store;

@Repository
@RequiredArgsConstructor
public class PushQueryDSLRepositoryImpl implements PushQueryDSLRepository {

    private final JPAQueryFactory queryFactory;


    @Override
    public Push findPush(Long boardId, String pushCategory, Long memberId) {
        return queryFactory.selectFrom(push)
                .where(commonFilter(memberId, boardId, pushCategory))
                .fetchFirst();
    }

    @Override
    public List<PushResponse> findPushList(Long boardId, String pushCategory, Long memberId) {
        return queryFactory.select(new QPushResponse(
                        store.name,
                        board.title,
                        board.profile,
                        push.pushStatus
                ))
                .from(push)
                .join(board).on(push.boardId.eq(board.id))
                .join(store).on(board.store.id.eq(store.id))
                .where(commonFilter(memberId, boardId, pushCategory))
                .fetch();
    }


    private BooleanBuilder commonFilter(Long memberId, Long boardId, String pushCategory) {
        BooleanBuilder builder = new BooleanBuilder();

        if (memberId != null) {
            builder.and(push.memberId.eq(memberId));
        }
        if (boardId != null) {
            builder.and(push.boardId.eq(boardId));
        }
        if (pushCategory != null) {
            builder.and(push.pushCategory.eq(PushCategory.valueOf(pushCategory)));
        }

        return builder;
    }

}
