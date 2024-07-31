package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberQueryDSLRepository {

    @Query("SELECT m.id FROM Member m WHERE m.provider = :provider AND m.providerId = :providerId AND m.isDeleted = false")
    Optional<Long> findByProviderAndProviderId(@Param("provider") OauthServerType provider,
        @Param("providerId") String providerId);
}
