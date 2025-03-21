package com.bbangle.bbangle.board.repository.util;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.NumberPath;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BoardFilterCreator {

    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;

    private final FilterRequest filterRequest;
    private final BooleanBuilder builder = new BooleanBuilder();

    public BooleanBuilder create() {
        addTagCondition();
        addPriceCondition();
        addCategoryCondition();
        addDaysOfWeekCondition();

        return builder;
    }

    private void addDaysOfWeekCondition() {
        if (filterRequest.orderAvailableToday() != null && filterRequest.orderAvailableToday()) {
            DayOfWeek dayOfWeek = LocalDate.now()
                .getDayOfWeek();

            switch (dayOfWeek) {
                case MONDAY -> builder.and(product.monday.eq(true));
                case TUESDAY -> builder.and(product.tuesday.eq(true));
                case WEDNESDAY -> builder.and(product.wednesday.eq(true));
                case THURSDAY -> builder.and(product.thursday.eq(true));
                case FRIDAY -> builder.and(product.friday.eq(true));
                case SATURDAY -> builder.and(product.saturday.eq(true));
                case SUNDAY -> builder.and(product.sunday.eq(true));
            }
        }
    }

    private void addCategoryCondition() {
        if (filterRequest.category() != null && filterRequest.category() == Category.ALL_BREAD) {
            builder.and(product.category.in(
                List.of(Category.BAGEL, Category.BREAD, Category.CAKE, Category.TART)));
            return;
        }

        if (filterRequest.category() != null && filterRequest.category() == Category.ALL_SNACK) {
            builder.and(product.category.in(
                List.of(Category.COOKIE, Category.SNACK, Category.JAM, Category.ICE_CREAM,
                    Category.YOGURT, Category.GRANOLA, Category.ETC)));
            return;
        }

        if (filterRequest.category() != null && filterRequest.category() != Category.ALL) {
            builder.and(product.category.eq(filterRequest.category()));
        }
    }

    private void addPriceCondition() {
        builder.and(goeExpression(board.price, filterRequest.minPrice()));
        builder.and(loeExpression(board.price, filterRequest.maxPrice()));
    }

    private void addTagCondition() {
        builder.and(eqExpression(product.glutenFreeTag, filterRequest.glutenFreeTag()));
        builder.and(eqExpression(product.highProteinTag, filterRequest.highProteinTag()));
        builder.and(eqExpression(product.sugarFreeTag, filterRequest.sugarFreeTag()));
        builder.and(eqExpression(product.veganTag, filterRequest.veganTag()));
        builder.and(eqExpression(product.ketogenicTag, filterRequest.ketogenicTag()));
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
