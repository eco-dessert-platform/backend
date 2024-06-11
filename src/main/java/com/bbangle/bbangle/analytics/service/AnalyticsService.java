package com.bbangle.bbangle.analytics.service;

import com.bbangle.bbangle.analytics.dto.AnalyticsCumulationResponseDto;
import com.bbangle.bbangle.analytics.dto.AnalyticsCreatedWithinPeriodResponseDto;
import com.bbangle.bbangle.analytics.dto.AnalyticsMembersCountResponseDto;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

        return AnalyticsCreatedWithinPeriodResponseDto.from(wishListBoardRepository.countWishlistCreatedBetweenPeriod(startLocalDate, endLocalDate));
    }


    @Transactional(readOnly = true)
    public AnalyticsCreatedWithinPeriodResponseDto analyzeReviewByPeriod(Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        return AnalyticsCreatedWithinPeriodResponseDto.from(reviewRepository.countReviewCreatedBetweenPeriod(startLocalDate, endLocalDate));
    }


    @Transactional(readOnly = true)
    public List<AnalyticsCumulationResponseDto> countCumulatedReviewsByPeriod(Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        LocalDate startLocalDate = startDate.orElse(LocalDate.now().minusDays(6));
        LocalDate endLocalDate = endDate.orElse(LocalDate.now());

        return reviewRepository.countCumulatedReviewBeforeEndDate(startLocalDate, endLocalDate);
    }
}