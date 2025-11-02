package com.bbangle.bbangle.review.repository;

import com.bbangle.bbangle.review.domain.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewQueryDSLRepository {

    @Query("SELECT r.id from Review r WHERE r.isBest = true")
    List<Long> getBestReviewIds();
}
