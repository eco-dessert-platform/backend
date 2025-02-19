package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.member.domain.SignatureAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SignatureAgreementRepository extends JpaRepository<SignatureAgreement, Long> {

    @Query("select count(s.id) > 0 from SignatureAgreement s where s.member.id = :memberId")
    boolean existsByMemberId(@Param("memberId") Long memberId);
}
