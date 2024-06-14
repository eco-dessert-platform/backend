package com.bbangle.bbangle.review.domain;


import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.review.dto.ReviewRequest;
import jakarta.persistence.*;
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

    //동현님 말대로 연관매핑 안해보고 해보기
    @Column(name = "member_id")
    @NotNull
    private Long memberId;

    @Column(name = "board_id")
    @NotNull
    private Long boardId;

    @NotNull
    @Column(name = "badge_taste", columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private Badge badgeTaste;

    @NotNull
    @Column(name = "badge_brix", columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private Badge badgeBrix;

    @NotNull
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

}
