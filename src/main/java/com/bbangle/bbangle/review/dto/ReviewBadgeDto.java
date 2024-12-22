package com.bbangle.bbangle.review.dto;

import com.bbangle.bbangle.review.domain.Badge;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewBadgeDto {
    private Badge badgeTaste;
    private Badge badgeBrix;
    private Badge badgeTexture;
}
