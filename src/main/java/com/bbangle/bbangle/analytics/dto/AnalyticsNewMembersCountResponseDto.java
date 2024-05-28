package com.bbangle.bbangle.analytics.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AnalyticsNewMembersCountResponseDto(
        List<AnalyticsMembersCountWithDateDto> membersCountDtos
) {
}
