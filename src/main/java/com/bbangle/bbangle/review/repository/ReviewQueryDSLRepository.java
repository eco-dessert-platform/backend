package com.bbangle.bbangle.review.repository;


import com.bbangle.bbangle.config.ranking.BoardGrade;
import com.bbangle.bbangle.review.domain.ReviewCursor;
import com.bbangle.bbangle.review.domain.ReviewLike;
import com.bbangle.bbangle.review.dto.LikeCountPerReviewIdDto;
import com.bbangle.bbangle.review.dto.ReviewCountPerBoardIdDto;
import com.bbangle.bbangle.review.dto.ReviewSingleDto;
import com.bbangle.bbangle.review.dto.ReviewImgDto;
import com.bbangle.bbangle.analytics.dto.AnalyticsCountWithDateResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReviewQueryDSLRepository {
    List<ReviewSingleDto> getReviewSingleList(Long boardId, Long cursorId);
    Map<Long, List<ReviewImgDto>> getImageMap(ReviewCursor reviewCursor);
    List<ReviewLike> getLikeList(ReviewCursor reviewCursor);
    ReviewSingleDto getReviewDetail(Long reviewId);
    List<ReviewSingleDto> getMyReviews(Long memberId, Long cursorId);
    List<ReviewImgDto> getAllImagesByBoardId(Long boardId, Long cursorId);
    List<ReviewCountPerBoardIdDto> getReviewCount();
    List<LikeCountPerReviewIdDto> getLikeCount(Long minimumBestReviewStandard);
    Map<Long, List<Long>> getBestReview(List<Long> reviewIds);
    void updateBestReview(List<Long> bestReviewIds);
    List<AnalyticsCountWithDateResponseDto> countMembersUsingReviewBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate);
    List<AnalyticsCountWithDateResponseDto> countReviewCreatedBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate);
    List<BoardGrade> groupByBoardIdAndGetReviewCountAndReviewRate();

}
