package com.bbangle.bbangle.review.repository;

import com.bbangle.bbangle.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewQueryDSLRepository {

}
