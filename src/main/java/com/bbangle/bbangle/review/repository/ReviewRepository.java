package com.bbangle.bbangle.review.repository;

import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.review.dto.ReviewDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewQueryDSLRepository {

    List<ReviewDto> findByBoardId(Long boardId);
}
