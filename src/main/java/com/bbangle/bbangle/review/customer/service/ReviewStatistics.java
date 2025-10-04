package com.bbangle.bbangle.review.customer.service;

import static com.bbangle.bbangle.review.domain.Badge.BAD;
import static com.bbangle.bbangle.review.domain.Badge.DRY;
import static com.bbangle.bbangle.review.domain.Badge.GOOD;
import static com.bbangle.bbangle.review.domain.Badge.PLAIN;
import static com.bbangle.bbangle.review.domain.Badge.SOFT;
import static com.bbangle.bbangle.review.domain.Badge.SWEET;

import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import com.bbangle.bbangle.review.domain.Badge;
import com.bbangle.bbangle.review.customer.dto.ReviewBadgeDto;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewStatistics {

    private static final int DEFAULT_VALUE = 0;
    private static final int INCREMENT_VALUE = 1;
    private static final String BLANK_BADGE = "blank badge";

    private final BoardStatisticRepository boardStatisticRepository;

    public BigDecimal getAverageRatingScore(Long boardId) {
        return boardStatisticRepository.findBoardReviewGradeByBoardId(boardId);
    }

    public int count(List<ReviewBadgeDto> reviews) {
        return reviews.size();
    }

    public List<String> getPopularBadgeList(List<ReviewBadgeDto> reviews) {
        return Stream.of(
                getPopularTasteBadge(reviews),
                getPopularBrixBadge(reviews),
                getPopularTextureBadge(reviews)
            )
            .filter(badge -> !badge.equals(BLANK_BADGE))
            .toList();
    }

    private String getPopularTasteBadge(List<ReviewBadgeDto> reviews) {
        return calculatePopularBadge(reviews, ReviewBadgeDto::getBadgeTaste, GOOD, BAD);
    }

    private String getPopularBrixBadge(List<ReviewBadgeDto> reviews) {
        return calculatePopularBadge(reviews, ReviewBadgeDto::getBadgeBrix, SWEET, PLAIN);
    }

    private String getPopularTextureBadge(List<ReviewBadgeDto> reviews) {
        return calculatePopularBadge(reviews, ReviewBadgeDto::getBadgeTexture, SOFT, DRY);
    }

    private String calculatePopularBadge(
        List<ReviewBadgeDto> reviews,
        Function<ReviewBadgeDto, Badge> badgeFunction,
        Badge positiveBadge,
        Badge nagativeBadge) {

        if (reviews.isEmpty()) {
            return BLANK_BADGE;
        }

        Map<Badge, Integer> badgeMap = new EnumMap<>(Badge.class);

        reviews.stream()
            .map(badgeFunction)
            .filter(badge ->
                badge != Badge.NULL
                    && (positiveBadge.equals(badge)
                    || nagativeBadge.equals(badge)))
            .forEach(badge -> badgeMap.merge(badge, INCREMENT_VALUE, Integer::sum));

        int positiveBadgeCount = badgeMap.getOrDefault(positiveBadge, DEFAULT_VALUE);
        int nagativeBadgeCount = badgeMap.getOrDefault(nagativeBadge, DEFAULT_VALUE);

        if (positiveBadgeCount == DEFAULT_VALUE && nagativeBadgeCount == DEFAULT_VALUE) {
            return BLANK_BADGE;
        }

        return positiveBadgeCount >= nagativeBadgeCount ? positiveBadge.name() : nagativeBadge.name();
    }
}
