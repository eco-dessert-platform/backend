package com.bbangle.bbangle.review.repository;

import com.bbangle.bbangle.analytics.dto.AnalyticsReviewUsageCountDto;

import java.time.LocalDate;
import java.util.List;

public interface ReviewQueryDSLRepository {

    List<AnalyticsReviewUsageCountDto> countMembersUsingReview(LocalDate startLocalDate, LocalDate endLocalDate);

    List<AnalyticsReviewUsageCountDto> countReviewByPeriod(LocalDate startLocalDate, LocalDate endLocalDate);
}
