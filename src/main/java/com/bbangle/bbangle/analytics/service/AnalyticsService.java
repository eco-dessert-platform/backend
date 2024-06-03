package com.bbangle.bbangle.analytics.service;

import com.bbangle.bbangle.analytics.dto.*;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    @Transactional(readOnly = true)
    public Long countAllMembers() {
        return memberRepository.count();
    }


    @Transactional(readOnly = true)
    public List<AnalyticsCountWithDateResponseDto> countMembersByPeriod(LocalDate startLocalDate, LocalDate endLocalDate) {
        return memberRepository.countMembersCreatedBetweenPeriod(startLocalDate, endLocalDate);
    }


    @Transactional(readOnly = true)
    public List<AnalyticsRatioWithDateResponseDto> calculateWishlistUsingRatio(LocalDate startLocalDate, LocalDate endLocalDate) {
        List<AnalyticsCountWithDateResponseDto> membersUsingWishlistDtos = wishListBoardRepository.countMembersUsingWishlist(startLocalDate, endLocalDate);
        List<AnalyticsCountWithDateResponseDto> membersCreatedBeforeDateDtos = memberRepository.countMembersCreatedBeforeEndDate(startLocalDate, endLocalDate);
        List<AnalyticsRatioWithDateResponseDto> ratioWithDateResponseDtos = new ArrayList<>();

        for (int i = 0; i < membersCreatedBeforeDateDtos.size(); i++) {
            if (membersCreatedBeforeDateDtos.get(i).count() == 0L) {
                throw new IllegalArgumentException("0으로 나눌 수 없습니다.");
            }

            double result = ((double) membersUsingWishlistDtos.get(i).count() / membersCreatedBeforeDateDtos.get(i).count()) * 100;
            String wishlistUsageRatio = String.format("%.2f", result);

            ratioWithDateResponseDtos.add(new AnalyticsRatioWithDateResponseDto(membersUsingWishlistDtos.get(i).date(), wishlistUsageRatio));
        }

        return ratioWithDateResponseDtos;
    }


    @Transactional(readOnly = true)
    public List<AnalyticsWishlistBoardRankingResponseDto> getWishlistBoardRanking() {
        return boardRepository.getWishlistRanking();
    }


    @Transactional(readOnly = true)
    public List<AnalyticsCountWithDateResponseDto> countWishlistBoardByPeriod(LocalDate startLocalDate, LocalDate endLocalDate) {
        return wishListBoardRepository.countWishlistByPeriod(startLocalDate, endLocalDate);
    }


    @Transactional(readOnly = true)
    public List<AnalyticsRatioWithDateResponseDto> calculateReviewUsingRatio(LocalDate startLocalDate, LocalDate endLocalDate) {
        List<AnalyticsCountWithDateResponseDto> reviewUsageCountDtos = reviewRepository.countMembersUsingReview(startLocalDate, endLocalDate);
        List<AnalyticsCountWithDateResponseDto> membersCreatedBeforeDateDtos = memberRepository.countMembersCreatedBeforeEndDate(startLocalDate, endLocalDate);
        List<AnalyticsRatioWithDateResponseDto> ratioWithDateResponseDtos = new ArrayList<>();

        for (int i = 0; i < membersCreatedBeforeDateDtos.size(); i++) {
            if (membersCreatedBeforeDateDtos.get(i).count() == 0L) {
                throw new IllegalArgumentException("0으로 나눌 수 없습니다.");
            }

            double result = ((double) reviewUsageCountDtos.get(i).count() / membersCreatedBeforeDateDtos.get(i).count()) * 100;
            String reviewUsageRatio = String.format("%.2f", result);

            ratioWithDateResponseDtos.add(new AnalyticsRatioWithDateResponseDto(reviewUsageCountDtos.get(i).date(), reviewUsageRatio));
        }

        return ratioWithDateResponseDtos;
    }


    @Transactional(readOnly = true)
    public List<AnalyticsCountWithDateResponseDto> countReviewByPeriod(LocalDate startLocalDate, LocalDate endLocalDate) {
        return reviewRepository.countReviewByPeriod(startLocalDate, endLocalDate);
    }

}
