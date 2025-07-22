package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.dto.MemberIdWithRoleDto;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import java.time.LocalDate;
import java.util.Optional;

public interface MemberQueryDSLRepository {

    Member findMemberById(Long memberId);

    Long countMembers();

    Long countMembersCreatedBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate);

    Optional<MemberIdWithRoleDto> findByProviderAndProviderId(OauthServerType provider, String providerId);

}