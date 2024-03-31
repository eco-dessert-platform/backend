package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findById(Long memberId);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    Optional<Member> findByProviderAndProviderId(OauthServerType provider, String providerId);

}
