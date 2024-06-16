package com.bbangle.bbangle.preference.repository;

import com.bbangle.bbangle.preference.domain.MemberPreference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberPreferenceRepository extends JpaRepository<MemberPreference, Long> {

    boolean existsByMemberId(Long memberId);

    Optional<MemberPreference> findByMemberId(Long memberId);

}
