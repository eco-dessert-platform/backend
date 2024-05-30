package com.bbangle.bbangle.review.repository;

import com.bbangle.bbangle.review.domain.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    Optional<ReviewLike> findByMemberIdAndReviewId(Long reviewId, Long memberId);

}
