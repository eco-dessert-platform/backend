package com.bbangle.bbangle.review.domain;


import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.review.customer.dto.ReviewRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Table(name = "review")
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    @NotNull
    private Long memberId;

    @Column(name = "board_id")
    @NotNull
    private Long boardId;

    @Column(name = "badge_taste", columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private Badge badgeTaste;

    @Column(name = "badge_brix", columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private Badge badgeBrix;

    @Column(name = "badge_texture", columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private Badge badgeTexture;

    @NotNull
    @Column(name = "rate")
    private BigDecimal rate;

    @Column(name = "is_best", columnDefinition = "tinyint")
    private Boolean isBest;

    @Column(name = "content")
    private String content;

    @Column(name = "is_deleted", columnDefinition = "tinyint")
    private boolean isDeleted;

    public void insertBadge(Badge badge) {
        switch (badge) {
            case GOOD, BAD -> this.badgeTaste = badge;
            case SWEET, PLAIN -> this.badgeBrix = badge;
            case SOFT, DRY -> this.badgeTexture = badge;
            case NULL -> {
                this.badgeTaste = badge;
                this.badgeBrix = badge;
                this.badgeTexture = badge;
            }
        }
    }

    public void update(ReviewRequest reviewRequest) {
        List<Badge> badges = reviewRequest.badges();
        for (Badge badge : badges) {
            this.insertBadge(badge);
        }
        this.rate = reviewRequest.rate();
        this.content = reviewRequest.content();
    }

    public void delete() {
        this.isDeleted = true;
    }

    public static Review of(ReviewRequest reviewRequest, Long memberId) {
        return Review.builder()
            .content(reviewRequest.content())
            .rate(reviewRequest.rate())
            .memberId(memberId)
            .boardId(reviewRequest.boardId())
            .isBest(false)
            .build();
    }

}
