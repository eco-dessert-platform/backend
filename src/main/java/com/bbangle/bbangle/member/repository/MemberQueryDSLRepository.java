package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.analytics.dto.AnalyticsMembersCountWithDateDto;
import com.bbangle.bbangle.member.domain.Member;

import java.time.LocalDate;
import java.util.List;

public interface MemberQueryDSLRepository {

    Member findMemberById(Long memberId);

    List<AnalyticsMembersCountWithDateDto> countMembersCreatedBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate);

    List<AnalyticsMembersCountWithDateDto> countMembersCreatedBeforeEndDate(LocalDate startLocalDate, LocalDate endLocalDate);

}
