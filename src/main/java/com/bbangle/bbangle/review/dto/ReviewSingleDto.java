package com.bbangle.bbangle.review.dto;

import com.bbangle.bbangle.review.domain.Badge;
import com.querydsl.core.annotations.QueryProjection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReviewSingleDto(
    Long id,
    String nickname,
    BigDecimal rate,
    Badge badgeTaste,
    Badge badgeBrix,
    Badge badgeTexture,
    String content,
    LocalDateTime createdAt,
    Boolean isBest
) {
    @QueryProjection
    public ReviewSingleDto { // Noncompliant - method is empty
    }
}
