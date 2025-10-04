package com.bbangle.bbangle.analytics.admin.dto;

import lombok.Builder;

@Builder
public record AnalyticsMembersCountResponseDto(
    Long count
) {

    public static AnalyticsMembersCountResponseDto from(Long count) {
        return new AnalyticsMembersCountResponseDto(count);
    }

}
