package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberQueryDSLRepository {

}
