package com.bbangle.bbangle.analytics.service;

import com.bbangle.bbangle.analytics.dto.AnalyticsMembersCountWithDateDto;
import com.bbangle.bbangle.analytics.dto.AnalyticsMembersUsingWishlistDto;
import com.bbangle.bbangle.analytics.dto.AnalyticsWishlistBoardRankingResponseDto;
import com.bbangle.bbangle.analytics.dto.AnalyticsWishlistUsageRatioResponseDto;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final WishListBoardRepository wishListBoardRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final BoardRepository boardRepository;


    public Long countAllMembers() {
        return memberRepository.count();
    }


    public List<AnalyticsMembersCountWithDateDto> countMembersByPeriod(LocalDate startLocalDate, LocalDate endLocalDate) {
        return memberRepository.countMembersCreatedBetweenPeriod(startLocalDate, endLocalDate);
    }


    public List<AnalyticsWishlistUsageRatioResponseDto> calculateWishlistUsingRatio(LocalDate startLocalDate, LocalDate endLocalDate) {
        List<AnalyticsMembersUsingWishlistDto> membersUsingWishlistDtos = wishListBoardRepository.countMembersUsingWishlist(startLocalDate, endLocalDate);
        List<AnalyticsMembersCountWithDateDto> membersCreatedBeforeDateDtos = memberRepository.countMembersCreatedBeforeEndDate(startLocalDate, endLocalDate);
        List<AnalyticsWishlistUsageRatioResponseDto> analyticsWishlistUsageRatioResponseDtos = new ArrayList<>();

        for (int i = 0; i < membersCreatedBeforeDateDtos.size(); i++) {
            if (membersCreatedBeforeDateDtos.get(i).count() == 0L) {
                throw new IllegalArgumentException("0으로 나눌 수 없습니다.");
            }

            double result = ((double) membersUsingWishlistDtos.get(i).count() / membersCreatedBeforeDateDtos.get(i).count()) * 100;
            String wishlistUsageRatio = String.format("%.2f", result);

            analyticsWishlistUsageRatioResponseDtos.add(new AnalyticsWishlistUsageRatioResponseDto(membersUsingWishlistDtos.get(i).date(), wishlistUsageRatio));
        }

        return analyticsWishlistUsageRatioResponseDtos;
    }


    public List<AnalyticsWishlistBoardRankingResponseDto> getWishlistBoardRanking() {
        return boardRepository.getWishlistRanking();
    }


    public Long countWishlistBoardByPeriod(LocalDate startLocalDate, LocalDate endLocalDate) {
        return wishListBoardRepository.countWishlistByPeriod(startLocalDate, endLocalDate);
    }


    public String calculateReviewUsingRatio(LocalDate startLocalDate, LocalDate endLocalDate) {
        Long reviewUsersCount = reviewRepository.countMembersUsingReview();
        long membersCount = memberRepository.count();

        if (membersCount == 0L) {
            throw new IllegalArgumentException("0으로 나눌 수 없습니다.");
        }

        double result = ((double) reviewUsersCount / membersCount) * 100;

        return String.format("%.2f", result);
    }


    public Long countReviewByPeriod(LocalDate startLocalDate, LocalDate endLocalDate) {
        return reviewRepository.countReviewByPeriod(startLocalDate, endLocalDate);
    }
}
