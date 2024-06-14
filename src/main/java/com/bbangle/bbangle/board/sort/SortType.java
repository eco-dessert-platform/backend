package com.bbangle.bbangle.board.sort;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.querydsl.core.types.OrderSpecifier;
import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SortType {
    RECENT("최신순", () -> List.of(QBoard.board.id.desc())),
    LOW_PRICE("낮은 가격순", () -> List.of(QBoard.board.price.asc(), QBoard.board.id.desc())),
    HIGH_PRICE("높은 가격순", () -> List.of(QBoard.board.price.desc(), QBoard.board.id.desc())),
    RECOMMEND("추천순", () -> List.of(QBoardStatistic.boardStatistic.basicScore.desc(), QBoard.board.id.desc())),
    MOST_WISHED("찜 많은 순", () -> List.of(QBoardStatistic.boardStatistic.boardWishCount.desc(), QBoard.board.id.desc())),
    MOST_REVIEWED("리뷰 많은 순", () -> List.of(QBoardStatistic.boardStatistic.basicScore.desc(), QBoard.board.id.desc())),
    HIGHEST_RATED("만족도 순", () -> List.of(QBoardStatistic.boardStatistic.basicScore.desc(), QBoard.board.id.desc()));

    private final String description;
    private final Supplier<List<OrderSpecifier<?>>> setOrder;

    public OrderSpecifier<?>[] getOrderExpression(){
        return setOrder.get().toArray(new OrderSpecifier[0]);
    }
}
