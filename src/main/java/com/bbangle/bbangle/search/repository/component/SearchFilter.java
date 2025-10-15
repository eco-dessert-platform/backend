package com.bbangle.bbangle.search.repository.component;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.customer.dto.FilterRequest;
import com.bbangle.bbangle.board.domain.MemberSegment;
import com.bbangle.bbangle.board.domain.constant.SortType;
import com.bbangle.bbangle.search.customer.service.dto.SearchInfo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.board.domain.QSegmentIntolerance.segmentIntolerance;
import static com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic.boardStatistic;

@Component
@RequiredArgsConstructor
public class SearchFilter {

        public BooleanExpression getLikeKeyword(String keyword) {
                if (Objects.isNull(keyword)) {
                        return null;
                }

                return board.title.like("%" + keyword + "%");
        }

        public BooleanExpression getDaysOfWeekCondition(FilterRequest filterRequest) {
                if (Objects.isNull(filterRequest.orderAvailableToday()) || Boolean.FALSE.equals(filterRequest.orderAvailableToday())) {
                        return null;
                }

                DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
                return switch (dayOfWeek) {
                        case MONDAY -> product.monday.eq(true);
                        case TUESDAY -> product.tuesday.eq(true);
                        case WEDNESDAY -> product.wednesday.eq(true);
                        case THURSDAY -> product.thursday.eq(true);
                        case FRIDAY -> product.friday.eq(true);
                        case SATURDAY -> product.saturday.eq(true);
                        case SUNDAY -> product.sunday.eq(true);
                };
        }

        public BooleanExpression getCategory(FilterRequest filterRequest) {
                if (Objects.isNull(filterRequest.category()) || filterRequest.category()
                    .equals(Category.ALL)) {
                        return null;
                }

                return product.category.eq(filterRequest.category());
        }

        public BooleanExpression getBetweenPrice(FilterRequest filterRequest) {
                if (Objects.nonNull(filterRequest.minPrice()) && Objects.nonNull(
                    filterRequest.maxPrice())) {
                        return board.price.goe(filterRequest.minPrice())
                            .and(board.price.loe(filterRequest.maxPrice()));
                }

                if (Objects.nonNull(filterRequest.minPrice())) {
                        return board.price.goe(filterRequest.minPrice());
                }

                if (Objects.nonNull(filterRequest.maxPrice())) {
                        return board.price.loe(filterRequest.maxPrice());
                }

                return null;
        }

        public BooleanBuilder getEqualTag(FilterRequest filterRequest) {
                BooleanBuilder builder = new BooleanBuilder();

                if (Boolean.TRUE.equals(filterRequest.glutenFreeTag())) {
                        builder.and(product.glutenFreeTag.eq(filterRequest.glutenFreeTag()));
                }

                if (Boolean.TRUE.equals(filterRequest.highProteinTag())) {
                        builder.and(product.highProteinTag.eq(filterRequest.highProteinTag()));
                }

                if (Boolean.TRUE.equals(filterRequest.sugarFreeTag())) {
                        builder.and(product.sugarFreeTag.eq(filterRequest.sugarFreeTag()));
                }

                if (Boolean.TRUE.equals(filterRequest.veganTag())) {
                        builder.and(product.veganTag.eq(filterRequest.veganTag()));
                }

                if (Boolean.TRUE.equals(filterRequest.ketogenicTag())) {
                        builder.and(product.ketogenicTag.eq(filterRequest.ketogenicTag()));
                }

                return builder;
        }

        public BooleanExpression getCursorCondition(SortType sortType,
                                                    SearchInfo.CursorCondition condition) {
                if (condition.getCursorId().equals(Long.MAX_VALUE)) {
                        return null;
                }

                if (sortType == SortType.LOW_PRICE) {
                        return board.price.gt(condition.getPrice())
                            .or(
                                board.price.eq(condition.getPrice())
                                    .and(board.id.loe(condition.getCursorId())));
                }

                if (sortType == SortType.HIGH_PRICE) {
                        return board.price.lt(condition.getPrice())
                            .or(
                                board.price.eq(condition.getPrice())
                                    .and(board.id.loe(condition.getCursorId())));
                }

                if (sortType == SortType.RECENT) {
                        return board.id.loe(condition.getCursorId());
                }

                if (sortType == SortType.HIGHEST_RATED) {
                        return boardStatistic.basicScore.lt(condition.getBoardBasicScore())
                            .or(
                                boardStatistic.basicScore.eq(condition.getBoardBasicScore())
                                    .and(board.id.loe(condition.getCursorId())));
                }

                if (sortType == SortType.MOST_WISHED) {
                        return boardStatistic.boardWishCount.lt(condition.getBoardWishedCount())
                            .or(
                                boardStatistic.boardWishCount.eq(condition.getBoardWishedCount())
                                    .and(board.id.loe(condition.getCursorId())));
                }

                if (sortType == SortType.MOST_REVIEWED) {
                        return boardStatistic.boardReviewCount.lt(condition.getBoardReviewCount())
                            .or(
                                boardStatistic.boardReviewCount.eq(condition.getBoardReviewCount())
                                    .and(board.id.loe(condition.getCursorId())));

                }

                return boardStatistic.basicScore.lt(condition.getBoardBasicScore())
                    .or(
                        boardStatistic.basicScore.eq(condition.getBoardBasicScore())
                            .and(board.id.loe(condition.getCursorId())));
        }

        public BooleanBuilder getExclusionCondition(MemberSegment memberSegment) {
                BooleanBuilder exclusionCondition = new BooleanBuilder();

                if (Boolean.TRUE.equals(memberSegment.getLactoseIntolerance())) {
                        exclusionCondition.and(segmentIntolerance.lactoseTag.eq(false));
                }
                if (Boolean.TRUE.equals(memberSegment.getPeanutIntolerance())) {
                        exclusionCondition.and(segmentIntolerance.peanutTag.eq(false));
                }
                if (Boolean.TRUE.equals(memberSegment.getPeachIntolerance())) {
                        exclusionCondition.and(segmentIntolerance.peachTag.eq(false));
                }
                if (Boolean.TRUE.equals(memberSegment.getRiceIntolerance())) {
                        exclusionCondition.and(segmentIntolerance.riceTag.eq(false));
                }
                if (Boolean.TRUE.equals(memberSegment.getTomatoIntolerance())) {
                        exclusionCondition.and(segmentIntolerance.tomatoTag.eq(false));
                }
                if (Boolean.TRUE.equals(memberSegment.getPineNutsIntolerance())) {
                        exclusionCondition.and(segmentIntolerance.pineNutsTag.eq(false));
                }
                if (Boolean.TRUE.equals(memberSegment.getSoyMilkIntolerance())) {
                        exclusionCondition.and(segmentIntolerance.soyMilkTag.eq(false));
                }
                if (Boolean.TRUE.equals(memberSegment.getBeanIntolerance())) {
                        exclusionCondition.and(segmentIntolerance.beanTag.eq(false));
                }
                if (Boolean.TRUE.equals(memberSegment.getWalnutsIntolerance())) {
                        exclusionCondition.and(segmentIntolerance.walnutsTag.eq(false));
                }

                return exclusionCondition;
        }

}
