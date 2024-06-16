package com.bbangle.bbangle.analytics.service;

import com.bbangle.bbangle.analytics.dto.AnalyticsCumulationResponseDto;
import com.bbangle.bbangle.analytics.dto.AnalyticsCreatedWithinPeriodResponseDto;
import com.bbangle.bbangle.analytics.dto.AnalyticsMembersCountResponseDto;
import com.bbangle.bbangle.analytics.dto.DateAndCountDto;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final WishListBoardRepository wishListBoardRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;


    @Transactional(readOnly = true)
    public AnalyticsMembersCountResponseDto countMembers() {
        return AnalyticsMembersCountResponseDto.from(memberRepository.countMembers());
    }


    @Transactional(readOnly = true)
    public AnalyticsMembersCountResponseDto countMembersByPeriod(Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        return AnalyticsMembersCountResponseDto.from(memberRepository.countMembersCreatedBetweenPeriod(startLocalDate, endLocalDate));
    }


    @Transactional(readOnly = true)
    public AnalyticsCreatedWithinPeriodResponseDto analyzeWishlistBoardByPeriod(Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        List<DateAndCountDto> rawResults = wishListBoardRepository.countWishlistCreatedBetweenPeriod(startLocalDate, endLocalDate);

        List<DateAndCountDto> results = mapResultsToDateRangeWithCount(startLocalDate, endLocalDate, rawResults,
                DateAndCountDto::date, DateAndCountDto::count, DateAndCountDto::new);

        Long total = rawResults.stream()
                .mapToLong(DateAndCountDto::count)
                .sum();
        Double daysBetween = calculateDaysBetween(startLocalDate, endLocalDate);

        Double rawAverage = (total / daysBetween);
        String average = String.format("%.2f", rawAverage);

        return AnalyticsCreatedWithinPeriodResponseDto.from(results, total, average);
    }


    @Transactional(readOnly = true)
    public AnalyticsCreatedWithinPeriodResponseDto analyzeReviewByPeriod(Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        List<DateAndCountDto> rawResults = reviewRepository.countReviewCreatedBetweenPeriod(startLocalDate, endLocalDate);

        List<DateAndCountDto> results = mapResultsToDateRangeWithCount(startLocalDate, endLocalDate, rawResults,
                DateAndCountDto::date, DateAndCountDto::count, DateAndCountDto::new);

        Long total = rawResults.stream()
                .mapToLong(DateAndCountDto::count)
                .sum();
        Double daysBetween = calculateDaysBetween(startLocalDate, endLocalDate);

        Double rawAverage = (total / daysBetween);
        String average = String.format("%.2f", rawAverage);

        return AnalyticsCreatedWithinPeriodResponseDto.from(results, total, average);
    }


    @Transactional(readOnly = true)
    public List<AnalyticsCumulationResponseDto> countCumulatedReviewsByPeriod(Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        List<AnalyticsCumulationResponseDto> rawResults = reviewRepository.countCumulatedReviewBeforeEndDate(startLocalDate, endLocalDate);

        return mapResultsToDateRangeWithCumulativeCount(startLocalDate, endLocalDate, rawResults,
                AnalyticsCumulationResponseDto::date, AnalyticsCumulationResponseDto::count, AnalyticsCumulationResponseDto::new);
    }


    private Double calculateDaysBetween(LocalDate startDate, LocalDate endDate) {
        return (double) (ChronoUnit.DAYS.between(startDate, endDate) + 1);
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


    private <T, R> List<R> mapResultsToDateRangeWithCumulativeCount(
            LocalDate startLocalDate, LocalDate endLocalDate, List<T> results,
            Function<T, Date> dateExtractor,
            Function<T, Long> countExtractor,
            BiFunction<Date, Long, R> constructor) {

        Map<Date, Long> mapResults = results.stream()
                .collect(Collectors.toMap(
                        dateExtractor,
                        countExtractor
                ));

        List<R> finalResults = new ArrayList<>();
        Long cumulativeCount = 0L;

        for (LocalDate localDate = startLocalDate; !localDate.isAfter(endLocalDate); localDate = localDate.plusDays(1)) {
            Date date = Date.valueOf(localDate);

            cumulativeCount += mapResults.getOrDefault(date, 0L);
            finalResults.add(constructor.apply(date, cumulativeCount));
        }

        return finalResults;
    }

}