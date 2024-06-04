package com.bbangle.bbangle.board.repository.query;

import com.bbangle.bbangle.board.dto.CursorInfo;
import com.bbangle.bbangle.common.sort.SortType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardQueryProviderResolver {

    private final JPAQueryFactory queryFactory;

    public BoardQueryProvider resolve(SortType sort, Long cursorId) {
        // FIXME: 스프린트에서 정렬정보 받아서 적절한 쿼리 provider 내려주도록 변경 필요
        return switch (getSortType(sort)) {
            case POPULAR -> new PopularBoardQueryProvider(queryFactory, cursorId);
            default -> new DefaultBoardQueryProvider(queryFactory, sort, cursorInfo);
        };
    }

    private SortType getSortType(SortType sort) {
        // FIXME: 정렬조건 없는경우는 컨트롤러에서 걸러주는게 나을듯함...
        return sort != null ? sort : SortType.RECOMMEND;
    }
}
