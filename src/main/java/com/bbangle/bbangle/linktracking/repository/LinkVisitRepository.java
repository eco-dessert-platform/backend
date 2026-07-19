package com.bbangle.bbangle.linktracking.repository;

import com.bbangle.bbangle.linktracking.domain.LinkVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface LinkVisitRepository extends JpaRepository<LinkVisit, Long> {

    boolean existsByTrackingLinkIdAndVisitorHashAndVisitDate(Long trackingLinkId, String visitorHash, LocalDate visitDate);
}
