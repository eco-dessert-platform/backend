package com.bbangle.bbangle.review.service;

import static com.bbangle.bbangle.review.domain.Badge.BAD;
import static com.bbangle.bbangle.review.domain.Badge.GOOD;
import static com.bbangle.bbangle.review.domain.Badge.HARD;
import static com.bbangle.bbangle.review.domain.Badge.PLAIN;
import static com.bbangle.bbangle.review.domain.Badge.SOFT;
import static com.bbangle.bbangle.review.domain.Badge.SWEET;
import static java.math.BigDecimal.ROUND_DOWN;

import com.bbangle.bbangle.review.domain.Badge;
import com.bbangle.bbangle.review.dto.ReviewDto;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class ReviewStatistics {

    public BigDecimal getAverageRatingScore(List<ReviewDto> reviews) {
        int reviewsSize = count(reviews);

        BigDecimal totalCount = new BigDecimal(reviewsSize);

        float ratingScore = 0f;
        for (ReviewDto review : reviews) {
            ratingScore += review.getRate().floatValue();
        }

        return new BigDecimal(ratingScore).divide(totalCount, 2, ROUND_DOWN);
    }

    public int count(List<ReviewDto> reviews) {
        return reviews.size();
    }

    public List<String> getPopularBadgeList(List<ReviewDto> reviews) {
        return List.of(
            getPopularTasteBadge(reviews),
            getPopularBrixBadge(reviews),
            getPopularTextureBadge(reviews));
    }

    private String getPopularTasteBadge(List<ReviewDto> reviews) {
        return calculatePopularBadge(reviews, ReviewDto::getBadgeTaste, GOOD, BAD);
    }

    private String getPopularBrixBadge(List<ReviewDto> reviews) {
        return calculatePopularBadge(reviews, ReviewDto::getBadgeBrix, SWEET, PLAIN);
    }

    private String getPopularTextureBadge(List<ReviewDto> reviews) {
        return calculatePopularBadge(reviews, ReviewDto::getBadgeTexture, SOFT, HARD);
    }

    private String calculatePopularBadge(
        List<ReviewDto> reviews,
        Function<ReviewDto, String> badgeFunction,
        Badge badge1,
        Badge badge2) {
        Map<String, Integer> badgeMap = new HashMap<>();

        int halfReviewsSize = count(reviews) / 2;

        for (ReviewDto review : reviews) {
            String badge = badgeFunction.apply(review);

            int badgeCount = badgeMap.getOrDefault(badge, 0);

            // 뱃지 카운트가 전체의 반 이상이면 인기 많은 뱃지로 반복문 조기 종료
            if (halfReviewsSize < badgeCount) {
                break;
            }

            badgeMap.put(badge, badgeCount + 1);
        }

        return badgeMap.getOrDefault(badge1.name(), 0) >= badgeMap.getOrDefault(badge2.name(), 0) ?
            badge1.name() : badge2.name();
    }
}
