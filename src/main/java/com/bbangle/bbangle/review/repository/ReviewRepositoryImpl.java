package com.bbangle.bbangle.review.repository;

import com.bbangle.bbangle.analytics.dto.AnalyticsReviewUsageCountDto;
import com.bbangle.bbangle.analytics.dto.QAnalyticsReviewUsageCountDto;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bbangle.bbangle.review.domain.QReview.review;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewQueryDSLRepository {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<AnalyticsReviewUsageCountDto> countMembersUsingReview(LocalDate startLocalDate, LocalDate endLocalDate) {
        DateTemplate<Date> createdAt = Expressions.dateTemplate(Date.class, "DATE({0})", review.createdAt);
        Date startDate = Date.valueOf(startLocalDate);
        Date endDate = Date.valueOf(endLocalDate);

        List<AnalyticsReviewUsageCountDto> results = queryFactory.select(new QAnalyticsReviewUsageCountDto(
                        createdAt,
                        review.memberId.countDistinct()
                ))
                .from(review)
                .where(createdAt.between(startDate, endDate))
                .groupBy(createdAt)
                .orderBy(createdAt.asc())
                .fetch();

        return mapResultsToDateRangeWithCount(startLocalDate, endLocalDate, results,
                AnalyticsReviewUsageCountDto::date, AnalyticsReviewUsageCountDto::reviewCount,
                AnalyticsReviewUsageCountDto::new);
    }


    @Override
    public List<AnalyticsReviewUsageCountDto> countReviewByPeriod(LocalDate startLocalDate, LocalDate endLocalDate) {
        DateTemplate<Date> createdAt = Expressions.dateTemplate(Date.class, "DATE({0})", review.createdAt);
        List<AnalyticsReviewUsageCountDto> mappedResults = new ArrayList<>();

        for (LocalDate date = startLocalDate; !date.isAfter(endLocalDate); date = date.plusDays(1)) {
            Long count = queryFactory.select(review.id.count())
                    .from(review)
                    .where(createdAt.loe(Date.valueOf(date)))
                    .fetchOne();

            mappedResults.add(new AnalyticsReviewUsageCountDto(Date.valueOf(date), count));
        }

        return mapResultsToDateRangeWithCount(startLocalDate, endLocalDate, mappedResults,
                AnalyticsReviewUsageCountDto::date, AnalyticsReviewUsageCountDto::reviewCount,
                AnalyticsReviewUsageCountDto::new);
    }


    private <T, R> List<R> mapResultsToDateRangeWithCount(
            LocalDate startLocalDate, LocalDate endLocalDate, List<T> results,
            Function<T, Date> dateExtractor,
            Function<T, Long> countExtractor,
            BiFunction<Date, Long, R> constructor) {

        Map<Date, Long> mapResults = results.stream()
                .collect(Collectors.toMap(
                        dateExtractor,
                        countExtractor
                ));

        List<LocalDate> dateRange = startLocalDate.datesUntil(endLocalDate.plusDays(1))
                .toList();

        return dateRange.stream()
                .map(date -> constructor.apply(Date.valueOf(date), mapResults.getOrDefault(Date.valueOf(date), 0L)))
                .toList();
    }

}
