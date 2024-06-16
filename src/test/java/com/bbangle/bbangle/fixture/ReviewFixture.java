package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.review.domain.Badge;
import com.bbangle.bbangle.review.domain.Review;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewFixture {

    public static Review createReviewWithBoardId(Long boardId) {
        return Review.builder()
            .boardId(boardId)
            .badgeBrix(Badge.BAD)
            .content("content")
            .rate(BigDecimal.valueOf(4.00))
            .build();
    }


    public static Review createReviewWithBoardIdAndRate(Long boardId, double rate) {
        return Review.builder()
            .boardId(boardId)
            .memberId(1L)
            .badgeBrix(Badge.DRY)
            .badgeTaste(Badge.GOOD)
            .badgeTexture(Badge.SOFT)
            .content("content")
            .rate(BigDecimal.valueOf(rate))
            .build();
    }

    public static Review createReview() {
        return Review.builder()
            .badgeBrix(Badge.BAD)
            .content("content")
            .rate(BigDecimal.valueOf(4.00))
            .build();
    }

    public static Review createReviewWithRate(BigDecimal rate) {
        return Review.builder()
            .badgeBrix(Badge.BAD)
            .content("content")
            .rate(rate)
            .build();
    }

}
