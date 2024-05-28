package com.bbangle.bbangle.wishlist.repository.impl;

import com.bbangle.bbangle.analytics.dto.AnalyticsMembersUsingWishlistDto;
import com.bbangle.bbangle.analytics.dto.QAnalyticsMembersUsingWishlistDto;
import com.bbangle.bbangle.wishlist.repository.WishListBoardQueryDSLRepository;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bbangle.bbangle.wishlist.domain.QWishListBoard.wishListBoard;

@Repository
@RequiredArgsConstructor
public class WishListBoardRepositoryImpl implements WishListBoardQueryDSLRepository {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<AnalyticsMembersUsingWishlistDto> countMembersUsingWishlist(LocalDate startLocalDate, LocalDate endLocalDate) {
        DateTemplate<Date> createdAt = Expressions.dateTemplate(Date.class, "DATE({0})", wishListBoard.createdAt);
        Date startDate = Date.valueOf(startLocalDate);
        Date endDate = Date.valueOf(endLocalDate);

        List<AnalyticsMembersUsingWishlistDto> results = queryFactory.select(new QAnalyticsMembersUsingWishlistDto(
                        createdAt,
                        wishListBoard.id.count()
                ))
                .from(wishListBoard)
                .where(createdAt.between(startDate, endDate))
                .groupBy(createdAt)
                .orderBy(createdAt.asc())
                .fetch();

        return mapResultsToDateRangeWithCount(startLocalDate, endLocalDate, results);
    }


    @Override
    public Long countWishlistByPeriod(LocalDate startDate, LocalDate endDate) {
        DateTemplate<LocalDate> createdAt = Expressions.dateTemplate(LocalDate.class, "DATE({0})", wishListBoard.createdAt);

        return queryFactory.select(wishListBoard.id.count())
                .from(wishListBoard)
                .where(createdAt.between(startDate, endDate))
                .fetchOne();
    }


    private static List<AnalyticsMembersUsingWishlistDto> mapResultsToDateRangeWithCount(LocalDate startLocalDate, LocalDate endLocalDate, List<AnalyticsMembersUsingWishlistDto> results) {
        Map<Date, Long> mapResults = results.stream()
                .collect(Collectors.toMap(
                        AnalyticsMembersUsingWishlistDto::date,
                        AnalyticsMembersUsingWishlistDto::count
                ));

        List<LocalDate> dateRange = startLocalDate.datesUntil(endLocalDate.plusDays(1))
                .toList();

        return dateRange.stream()
                .map(date -> new AnalyticsMembersUsingWishlistDto(Date.valueOf(date), mapResults.getOrDefault(Date.valueOf(date), 0L)))
                .toList();
    }

}
