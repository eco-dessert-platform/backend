package com.bbangle.bbangle.review.repository;

import com.bbangle.bbangle.analytics.dto.AnalyticsCountWithDateResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface ReviewQueryDSLRepository {

    List<AnalyticsCountWithDateResponseDto> countMembersUsingReview(LocalDate startLocalDate, LocalDate endLocalDate);

    List<AnalyticsCountWithDateResponseDto> countReviewByPeriod(LocalDate startLocalDate, LocalDate endLocalDate);
}
