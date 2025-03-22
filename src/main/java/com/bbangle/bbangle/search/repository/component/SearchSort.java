package com.bbangle.bbangle.search.repository.component;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.search.service.dto.SearchInfo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic.boardStatistic;

@Component
@RequiredArgsConstructor
public class SearchSort {

    public OrderSpecifier<?>[] getSortType(SortType sortType) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        switch (sortType) {
            case RECENT -> orders.add(board.id.desc());
            case LOW_PRICE -> orders.add(board.price.asc());
            case HIGH_PRICE -> orders.add(board.price.desc());
            case MOST_WISHED -> orders.add(boardStatistic.boardWishCount.desc());
            case MOST_REVIEWED -> orders.add(boardStatistic.boardReviewCount.desc());
            default -> orders.add(boardStatistic.basicScore.desc());
        }

        orders.add(board.id.desc());

        return orders.toArray(new OrderSpecifier[0]);
    }

}
