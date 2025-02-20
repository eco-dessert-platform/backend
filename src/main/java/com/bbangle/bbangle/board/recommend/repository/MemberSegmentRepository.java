package com.bbangle.bbangle.board.recommend.repository;

import com.bbangle.bbangle.board.recommend.domain.MemberSegment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberSegmentRepository extends JpaRepository<MemberSegment, Long> {

    Optional<MemberSegment> findByMemberId(Long memberId);

}
