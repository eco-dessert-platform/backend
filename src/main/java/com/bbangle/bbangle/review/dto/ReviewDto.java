package com.bbangle.bbangle.review.dto;

import com.bbangle.bbangle.review.domain.Badge;
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
}
