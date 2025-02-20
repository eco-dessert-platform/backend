package com.bbangle.bbangle.review.repository;


import com.bbangle.bbangle.analytics.dto.AnalyticsCumulationResponseDto;
import com.bbangle.bbangle.analytics.dto.DateAndCountDto;
import com.bbangle.bbangle.board.dto.AiLearningReviewDto;
import com.bbangle.bbangle.boardstatistic.ranking.BoardGrade;
import com.bbangle.bbangle.review.dao.ReviewStatisticDao;
import com.bbangle.bbangle.review.domain.ReviewCursor;
import com.bbangle.bbangle.review.domain.ReviewLike;
import com.bbangle.bbangle.image.dto.ImageDto;
import com.bbangle.bbangle.review.dto.LikeCountPerReviewIdDto;
import com.bbangle.bbangle.review.dto.ReviewBadgeDto;
import com.bbangle.bbangle.review.dto.ReviewCountPerBoardIdDto;
import com.bbangle.bbangle.review.dto.ReviewDto;
import com.bbangle.bbangle.review.dto.ReviewSingleDto;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReviewQueryDSLRepository {
    List<ReviewSingleDto> getReviewSingleList(Long boardId, Long cursorId);
    Map<Long, List<ImageDto>> getImageMap(ReviewCursor reviewCursor);
    List<ReviewLike> getLikeList(ReviewCursor reviewCursor);
    ReviewSingleDto getReviewDetail(Long reviewId);
    List<ReviewSingleDto> getMyReviews(Long memberId, Long cursorId);
    List<ImageDto> getAllImagesByBoardId(Long boardId, Long cursorId);
    List<ReviewCountPerBoardIdDto> getReviewCount();
    List<LikeCountPerReviewIdDto> getLikeCount(Long minimumBestReviewStandard);
    Map<Long, List<Long>> getBestReview(List<Long> reviewIds);
    @Transactional
    void updateBestReview(List<Long> bestReviewIds);
    List<BoardGrade> groupByBoardIdAndGetReviewCountAndReviewRate();
    List<DateAndCountDto> countReviewCreatedBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate);
    List<AnalyticsCumulationResponseDto> countCumulatedReviewBeforeEndDate(LocalDate startLocalDate, LocalDate endLocalDate);
    List<ReviewDto> findByBoardId(Long boardId);

    List<ReviewBadgeDto> findReviewBadgeByBoardId(Long boardId);

    List<ReviewStatisticDao> getReviewStatisticByBoardIds(List<Long> boardReviewUpdateId);

    List<AiLearningReviewDto> findAiLearningData(Integer offset, Integer limit);

}
