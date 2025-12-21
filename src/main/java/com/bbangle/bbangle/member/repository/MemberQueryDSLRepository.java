package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.member.domain.Member;
import java.time.LocalDate;
import java.util.Optional;

public interface MemberQueryDSLRepository {

    Member findMemberById(Long memberId);

    Long countMembers();

    Long countMembersCreatedBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate);

    Optional<Long> findByProviderAndProviderId(OauthServerType provider,
                                               String providerId);

}
