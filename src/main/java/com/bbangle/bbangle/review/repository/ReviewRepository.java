package com.bbangle.bbangle.review.repository;

import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.review.dto.ReviewDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewQueryDSLRepository {

    List<ReviewDto> findByBoardId(Long boardId);
    @Query("SELECT r.id from Review r WHERE r.isBest = true")
    List<Long> getBestReviewIds();
}
