package com.bbangle.bbangle.review.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
    @Column(name = "badge_taste")
    private String badgeTaste;
    @NotNull
    @Column(name = "badge_brix")
    private String badgeBrix;
    @NotNull
    @Column(name = "badge_texture")
    private String badgeTexture;
    @NotNull
    private BigDecimal rate;

    private String content;
    
    @Column(name = "is_deleted", columnDefinition = "tinyint")
    private boolean isDeleted;

    public void insertBadge(Badge badge) {
        switch (badge) {
            case GOOD, BAD -> this.badgeTaste = badge.name();
            case SWEET, PLAIN -> this.badgeBrix = badge.name();
            case SOFT, HARD -> this.badgeTexture = badge.name();
        }
    }
}
