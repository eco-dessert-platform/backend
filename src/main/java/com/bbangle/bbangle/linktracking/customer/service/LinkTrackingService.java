package com.bbangle.bbangle.linktracking.customer.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.linktracking.customer.service.command.LinkTrackingCommand;
import com.bbangle.bbangle.linktracking.domain.LinkVisit;
import com.bbangle.bbangle.linktracking.domain.TrackingLink;
import com.bbangle.bbangle.linktracking.repository.LinkVisitRepository;
import com.bbangle.bbangle.linktracking.repository.TrackingLinkRepository;
import com.bbangle.bbangle.util.VisitorFingerprintUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LinkTrackingService {

    private final TrackingLinkRepository trackingLinkRepository;
    private final LinkVisitRepository linkVisitRepository;

    @Value("${link-tracking.destinations}")
    private String destinationUrl;

    @Transactional
    public String resolveAndRecordVisit(LinkTrackingCommand.Visit command) {
        TrackingLink link = trackingLinkRepository.findByChannel(command.channel())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.TRACKING_LINK_NOT_FOUND));

        String visitorHash = VisitorFingerprintUtils.hash(command.ipAddress(), command.userAgent());
        LocalDate today = LocalDate.now();

        boolean isDuplicate = linkVisitRepository.existsByTrackingLinkIdAndVisitorHashAndVisitDate(link.getId(), visitorHash, today);

        LinkVisit visit = LinkVisit.create(
            link.getId(),
            visitorHash,
            today,
            isDuplicate,
            command.referer(),
            command.userAgent());

        linkVisitRepository.save(visit);

        return destinationUrl;
    }
}
