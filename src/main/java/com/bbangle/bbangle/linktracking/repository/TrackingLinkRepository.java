package com.bbangle.bbangle.linktracking.repository;

import com.bbangle.bbangle.linktracking.domain.LinkChannel;
import com.bbangle.bbangle.linktracking.domain.TrackingLink;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingLinkRepository extends JpaRepository<TrackingLink, Long> {

    Optional<TrackingLink> findByChannel(LinkChannel channel);
}
