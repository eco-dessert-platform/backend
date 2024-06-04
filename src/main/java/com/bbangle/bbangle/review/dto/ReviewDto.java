package com.bbangle.bbangle.review.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewDto {

    private Long id;
    private Long memberId;
    private Long boardId;
    private String badgeTaste;
    private String badgeBrix;
    private String badgeTexture;
    private BigDecimal rate;
    private String content;

    public ReviewDto(String badgeTaste, String badgeBrix, String badgeTexture, BigDecimal rate) {
        this.badgeTaste = badgeTaste;
        this.badgeBrix = badgeBrix;
        this.badgeTexture = badgeTexture;
        this.rate = rate;
    }
}
