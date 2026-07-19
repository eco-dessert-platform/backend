package com.bbangle.bbangle.linktracking.domain;

import com.bbangle.bbangle.common.domain.CreatedAtBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(
    name = "link_visit",
    indexes = {
        @Index(name = "idx_link_visit_dedup",
            columnList = "tracking_link_id, visitor_hash, visit_date"),
        @Index(name = "idx_link_visit_link_date",
            columnList = "tracking_link_id, visit_date")
    }
)
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinkVisit extends CreatedAtBaseEntity {

    private static final int MAX_HEADER_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "tracking_link_id", nullable = false)
    private Long trackingLinkId;

    @NotNull
    @Column(name = "visitor_hash", length = 64, nullable = false)
    private String visitorHash;

    @NotNull
    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @NotNull
    @Column(name = "is_duplicate", nullable = false, columnDefinition = "tinyint default 0")
    private boolean isDuplicate;

    @Column(name = "referer", length = MAX_HEADER_LENGTH)
    private String referer;

    @Column(name = "user_agent", length = MAX_HEADER_LENGTH)
    private String userAgent;

    private LinkVisit(
        Long trackingLinkId,
        String visitorHash,
        LocalDate visitDate,
        boolean isDuplicate,
        String referer,
        String userAgent
    ) {
        this.trackingLinkId = trackingLinkId;
        this.visitorHash = visitorHash;
        this.visitDate = visitDate;
        this.isDuplicate = isDuplicate;
        this.referer = referer;
        this.userAgent = userAgent;
    }

    public static LinkVisit create(
        Long trackingLinkId,
        String visitorHash,
        LocalDate visitDate,
        boolean isDuplicate,
        String referer,
        String userAgent
    ) {
        return new LinkVisit(trackingLinkId, visitorHash, visitDate, isDuplicate, referer, userAgent);
    }
}
