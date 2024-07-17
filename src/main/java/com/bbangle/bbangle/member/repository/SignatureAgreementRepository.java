package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.member.domain.SignatureAgreement;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SignatureAgreementRepository extends JpaRepository<SignatureAgreement, Long> {

    @Query("select count(s.id) > 0 from SignatureAgreement s where s.member.id = :memberId")
    boolean existsByMemberId(Long memberId);
}
