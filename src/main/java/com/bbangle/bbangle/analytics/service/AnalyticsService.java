package com.bbangle.bbangle.analytics.service;

import com.bbangle.bbangle.analytics.dto.*;
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
        List<AnalyticsWishlistUsageRatioResponseDto> wishlistUsageRatioResponseDtos = new ArrayList<>();

        for (int i = 0; i < membersCreatedBeforeDateDtos.size(); i++) {
            if (membersCreatedBeforeDateDtos.get(i).count() == 0L) {
                throw new IllegalArgumentException("0으로 나눌 수 없습니다.");
            }

            double result = ((double) membersUsingWishlistDtos.get(i).count() / membersCreatedBeforeDateDtos.get(i).count()) * 100;
            String wishlistUsageRatio = String.format("%.2f", result);

            wishlistUsageRatioResponseDtos.add(new AnalyticsWishlistUsageRatioResponseDto(membersUsingWishlistDtos.get(i).date(), wishlistUsageRatio));
        }

        return wishlistUsageRatioResponseDtos;
    }


    public List<AnalyticsWishlistBoardRankingResponseDto> getWishlistBoardRanking() {
        return boardRepository.getWishlistRanking();
    }


    public List<AnalyticsWishlistUsageCountResponseDto> countWishlistBoardByPeriod(LocalDate startLocalDate, LocalDate endLocalDate) {
        return wishListBoardRepository.countWishlistByPeriod(startLocalDate, endLocalDate);
    }


    public List<AnalyticsReviewUsageCountResponseDto> calculateReviewUsingRatio(LocalDate startLocalDate, LocalDate endLocalDate) {
        List<AnalyticsReviewUsageCountDto> reviewUsageCountDtos = reviewRepository.countMembersUsingReview(startLocalDate, endLocalDate);
        List<AnalyticsMembersCountWithDateDto> membersCreatedBeforeDateDtos = memberRepository.countMembersCreatedBeforeEndDate(startLocalDate, endLocalDate);
        List<AnalyticsReviewUsageCountResponseDto> reviewUsageCountResponseDtos = new ArrayList<>();

        for (int i = 0; i < membersCreatedBeforeDateDtos.size(); i++) {
            if (membersCreatedBeforeDateDtos.get(i).count() == 0L) {
                throw new IllegalArgumentException("0으로 나눌 수 없습니다.");
            }

            double result = ((double) reviewUsageCountDtos.get(i).reviewCount() / membersCreatedBeforeDateDtos.get(i).count()) * 100;
            String reviewUsageRatio = String.format("%.2f", result);

            reviewUsageCountResponseDtos.add(new AnalyticsReviewUsageCountResponseDto(reviewUsageCountDtos.get(i).date(), reviewUsageRatio));
        }

        return reviewUsageCountResponseDtos;
    }


    public List<AnalyticsReviewUsageCountDto> countReviewByPeriod(LocalDate startLocalDate, LocalDate endLocalDate) {
        return reviewRepository.countReviewByPeriod(startLocalDate, endLocalDate);
    }
}
