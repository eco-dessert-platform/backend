package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.analytics.dto.AnalyticsCountWithDateResponseDto;
import com.bbangle.bbangle.member.domain.Member;

import java.time.LocalDate;
import java.util.List;

public interface MemberQueryDSLRepository {

    Member findMemberById(Long memberId);

    List<AnalyticsCountWithDateResponseDto> countMembersCreatedBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate);

    List<AnalyticsCountWithDateResponseDto> countMembersCreatedBeforeEndDate(LocalDate startLocalDate, LocalDate endLocalDate);

}
