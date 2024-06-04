package com.bbangle.bbangle.review.repository;

import com.bbangle.bbangle.review.dto.ReviewDto;
import java.util.List;

public interface ReviewQueryDSLRepository {

    List<ReviewDto> findByBoardId(Long boardId);
}
