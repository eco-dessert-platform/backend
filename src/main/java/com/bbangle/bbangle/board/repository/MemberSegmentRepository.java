package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.MemberSegment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberSegmentRepository extends JpaRepository<MemberSegment, Long> {

    Optional<MemberSegment> findByMemberId(Long memberId);

}
