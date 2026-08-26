package com.bbangle.bbangle.member.repository;

import com.bbangle.bbangle.member.domain.Member;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberQueryDSLRepository {

    // 기본 배송지 변경 시 동시 insert 방지를 위한 직렬화 포인트 (Member 행 잠금)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.id = :memberId")
    Optional<Member> findByIdWithLock(@Param("memberId") Long memberId);
}
