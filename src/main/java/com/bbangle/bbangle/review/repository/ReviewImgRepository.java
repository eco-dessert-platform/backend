package com.bbangle.bbangle.review.repository;

import com.bbangle.bbangle.review.domain.ReviewImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImgRepository extends JpaRepository<ReviewImg, Long> {
    List<ReviewImg> findByReviewId(Long reviewId);
}
