package com.bbangle.bbangle.analytics.service;

import com.bbangle.bbangle.analytics.dto.AnalyticsCountWithDateResponseDto;
import com.bbangle.bbangle.analytics.dto.AnalyticsMembersCountResponseDto;
import com.bbangle.bbangle.analytics.dto.AnalyticsRatioWithDateResponseDto;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.bbangle.bbangle.exception.BbangleErrorCode.ZERO_REGISTERED_USERS;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final WishListBoardRepository wishListBoardRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;


    @Transactional(readOnly = true)
    public AnalyticsMembersCountResponseDto countAllMembers() {
        return AnalyticsMembersCountResponseDto.from(memberRepository.countMembers());
    }


    @Transactional(readOnly = true)
    public List<AnalyticsCountWithDateResponseDto> countMembersByPeriod(Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        List<AnalyticsCountWithDateResponseDto> results = memberRepository.countMembersCreatedBetweenPeriod(startLocalDate, endLocalDate);
        return mapResultsToDateRangeWithCount(startLocalDate, endLocalDate, results);
    }


    @Transactional(readOnly = true)
    public List<AnalyticsRatioWithDateResponseDto> calculateWishlistUsingRatio(Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        List<AnalyticsCountWithDateResponseDto> membersUsingWishlist = wishListBoardRepository.countMembersUsingWishlistBetweenPeriod(startLocalDate, endLocalDate);
        List<AnalyticsCountWithDateResponseDto> results = memberRepository.countMembersCreatedBeforeEndDate(startLocalDate, endLocalDate);
        List<AnalyticsCountWithDateResponseDto> membersCreatedBeforeDate = mapResultsToDateRangeWithCount(startLocalDate, endLocalDate, results);
        List<AnalyticsRatioWithDateResponseDto> ratioWithDateResponse = new ArrayList<>();

        calculateRatio(membersUsingWishlist, membersCreatedBeforeDate, ratioWithDateResponse);

        return ratioWithDateResponse;
    }


    @Transactional(readOnly = true)
    public List<AnalyticsCountWithDateResponseDto> countWishlistBoardByPeriod(Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        return wishListBoardRepository.countWishlistCreatedBetweenPeriod(startLocalDate, endLocalDate);
    }


    @Transactional(readOnly = true)
    public List<AnalyticsRatioWithDateResponseDto> calculateReviewUsingRatio(Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        List<AnalyticsCountWithDateResponseDto> membersUsingReview = reviewRepository.countMembersUsingReviewBetweenPeriod(startLocalDate, endLocalDate);
        List<AnalyticsCountWithDateResponseDto> results = memberRepository.countMembersCreatedBeforeEndDate(startLocalDate, endLocalDate);
        List<AnalyticsCountWithDateResponseDto> membersCreatedBeforeDate = mapResultsToDateRangeWithCount(startLocalDate, endLocalDate, results);
        List<AnalyticsRatioWithDateResponseDto> ratioWithDateResponse = new ArrayList<>();

        calculateRatio(membersUsingReview, membersCreatedBeforeDate, ratioWithDateResponse);

        return ratioWithDateResponse;
    }


    @Transactional(readOnly = true)
    public List<AnalyticsCountWithDateResponseDto> countReviewByPeriod(Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        return reviewRepository.countReviewCreatedBetweenPeriod(startLocalDate, endLocalDate);
    }


    private List<AnalyticsCountWithDateResponseDto> mapResultsToDateRangeWithCount(LocalDate startLocalDate, LocalDate endLocalDate, List<AnalyticsCountWithDateResponseDto> results) {
        Map<Date, Long> mapResults = results.stream()
                .collect(Collectors.toMap(
                        AnalyticsCountWithDateResponseDto::date,
                        AnalyticsCountWithDateResponseDto::count
                ));

        List<LocalDate> dateRange = startLocalDate.datesUntil(endLocalDate.plusDays(1))
                .toList();

        return dateRange.stream()
                .map(date -> new AnalyticsCountWithDateResponseDto(Date.valueOf(date), mapResults.getOrDefault(Date.valueOf(date), 0L)))
                .toList();
    }


    private void calculateRatio(List<AnalyticsCountWithDateResponseDto> reviewUsageCount, List<AnalyticsCountWithDateResponseDto> membersCreatedBeforeDate, List<AnalyticsRatioWithDateResponseDto> ratioWithDateResponse) {
        for (int i = 0; i < membersCreatedBeforeDate.size(); i++) {
            if (membersCreatedBeforeDate.get(i).count() == 0L) {
                throw new BbangleException(ZERO_REGISTERED_USERS);
            }

            double result = ((double) reviewUsageCount.get(i).count() / membersCreatedBeforeDate.get(i).count()) * 100;
            String reviewUsageRatio = String.format("%.2f", result);

            ratioWithDateResponse.add(new AnalyticsRatioWithDateResponseDto(reviewUsageCount.get(i).date(), reviewUsageRatio));
        }
    }

}