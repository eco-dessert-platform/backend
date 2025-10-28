package com.bbangle.bbangle.review.customer.dto;

import com.bbangle.bbangle.review.domain.Badge;
import com.querydsl.core.annotations.QueryProjection;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewDto {

    private Long id;
    private Long memberId;
    private Long boardId;
    private Badge badgeTaste;
    private Badge badgeBrix;
    private Badge badgeTexture;
    private BigDecimal rate;
    private String content;

    @QueryProjection
    public ReviewDto(Badge badgeTaste, Badge badgeBrix, Badge badgeTexture, BigDecimal rate) {
        this.badgeTaste = badgeTaste;
        this.badgeBrix = badgeBrix;
        this.badgeTexture = badgeTexture;
        this.rate = rate;
    }
}
