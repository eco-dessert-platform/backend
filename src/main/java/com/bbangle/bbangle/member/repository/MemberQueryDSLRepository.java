package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.member.domain.Member;

import java.time.LocalDate;

public interface MemberQueryDSLRepository {

    Member findMemberById(Long memberId);
    Long countMembers();
    Long countMembersCreatedBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate);

}