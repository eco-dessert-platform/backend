package com.bbangle.bbangle.search.repository.basic;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.NumberPath;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SearchFilterCreator {

    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;

    private final FilterRequest filterRequest;
    private final BooleanBuilder builder = new BooleanBuilder();

    public BooleanBuilder create() {
        addTagCondition();
        addPriceCondition();
        addCategoryCondition();
        addIsDeletedCondition();

        return builder;
    }

    private void addCategoryCondition() {
        if (filterRequest.category() != null && filterRequest.category() != Category.ALL) {
            builder.and(product.category.eq(filterRequest.category()));
        }
    }

    private void addPriceCondition() {
        if (Objects.nonNull(filterRequest.minPrice())) {
            builder.and(goeExpression(board.price, filterRequest.minPrice()));
        }

        if (Objects.nonNull(filterRequest.maxPrice())) {
            builder.and(loeExpression(board.price, filterRequest.maxPrice()));
        }
    }

    private void addTagCondition() {
        if (Boolean.TRUE.equals(filterRequest.glutenFreeTag())) {
            builder.and(eqExpression(product.glutenFreeTag, filterRequest.glutenFreeTag()));
        }

        if (Boolean.TRUE.equals(filterRequest.highProteinTag())) {
            builder.and(eqExpression(product.highProteinTag, filterRequest.highProteinTag()));
        }

        if (Boolean.TRUE.equals(filterRequest.sugarFreeTag())) {
            builder.and(eqExpression(product.sugarFreeTag, filterRequest.sugarFreeTag()));
        }

        if (Boolean.TRUE.equals(filterRequest.veganTag())) {
            builder.and(eqExpression(product.veganTag, filterRequest.veganTag()));
        }

        if (Boolean.TRUE.equals(filterRequest.ketogenicTag())) {
            builder.and(eqExpression(product.ketogenicTag, filterRequest.ketogenicTag()));
        }
    }

    private void addIsDeletedCondition() {
        builder.and(board.isDeleted.eq(false));
    }

    private BooleanExpression eqExpression(BooleanPath path, Boolean value) {
        return value == null ? null : path.eq(value);
    }

    private BooleanExpression goeExpression(NumberPath<Integer> path, Integer value) {
        return value == null ? null : path.goe(value);
    }

    private BooleanExpression loeExpression(NumberPath<Integer> path, Integer value) {
        return value == null ? null : path.loe(value);
    }
}
