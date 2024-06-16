package com.bbangle.bbangle.preference.repository;

import com.bbangle.bbangle.preference.domain.MemberPreference;
import com.bbangle.bbangle.preference.domain.Preference;
import com.bbangle.bbangle.preference.domain.PreferenceType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PreferenceRepository extends JpaRepository<Preference, Long>, PreferenceQueryDSLRepository {

    Optional<Preference> findByPreferenceType(PreferenceType preferenceType);

    @Query("select p from Preference p join MemberPreference mp on mp.preferenceId = p.id where mp = :memberPreference")
    Optional<Preference> findPreferenceTypeWithMemberPreference(MemberPreference memberPreference);
}
