package com.bbangle.bbangle.review.repository;

import com.bbangle.bbangle.review.domain.ReviewCursor;
import com.bbangle.bbangle.review.domain.ReviewLike;
import com.bbangle.bbangle.review.dto.ReviewSingleDto;

import java.util.List;
import java.util.Map;

public interface ReviewQueryDSLRepository {
    List<ReviewSingleDto> getReviewSingleList(Long boardId, Long cursorId);
    Map<Long, List<String>> getImageMap(ReviewCursor reviewCursor);
    List<ReviewLike> getLikeList(ReviewCursor reviewCursor);
    ReviewSingleDto getReviewDetail(Long reviewId, Long memberId);
    List<ReviewSingleDto> getMyReviews(Long memberId, Long cursorId);
}
