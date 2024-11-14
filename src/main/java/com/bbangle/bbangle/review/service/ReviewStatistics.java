package com.bbangle.bbangle.review.service;

import static com.bbangle.bbangle.review.domain.Badge.BAD;
import static com.bbangle.bbangle.review.domain.Badge.DRY;
import static com.bbangle.bbangle.review.domain.Badge.GOOD;
import static com.bbangle.bbangle.review.domain.Badge.PLAIN;
import static com.bbangle.bbangle.review.domain.Badge.SOFT;
import static com.bbangle.bbangle.review.domain.Badge.SWEET;

import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import com.bbangle.bbangle.review.domain.Badge;
import com.bbangle.bbangle.review.dto.ReviewDto;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewStatistics {

    private static final int DEFAULT_VALUE = 0;
    private static final int INCREMENT_VALUE = 1;
    private static final String NULL = null;

    private final BoardStatisticRepository boardStatisticRepository;

    public BigDecimal getAverageRatingScore(Long boardId) {
        return boardStatisticRepository.findBoardReviewGradeByBoardId(boardId);
    }

    public int count(List<ReviewDto> reviews) {
        return reviews.size();
    }

    public List<String> getPopularBadgeList(List<ReviewDto> reviews) {
        return Stream.of(
                getPopularTasteBadge(reviews),
                getPopularBrixBadge(reviews),
                getPopularTextureBadge(reviews)
            )
            .filter(Objects::nonNull)
            .toList();
    }

    private String getPopularTasteBadge(List<ReviewDto> reviews) {
        return calculatePopularBadge(reviews, ReviewDto::getBadgeTaste, GOOD, BAD);
    }

    private String getPopularBrixBadge(List<ReviewDto> reviews) {
        return calculatePopularBadge(reviews, ReviewDto::getBadgeBrix, SWEET, PLAIN);
    }

    private String getPopularTextureBadge(List<ReviewDto> reviews) {
        return calculatePopularBadge(reviews, ReviewDto::getBadgeTexture, SOFT, DRY);
    }

    private String calculatePopularBadge(
        List<ReviewDto> reviews,
        Function<ReviewDto, Badge> badgeFunction,
        Badge badge1,
        Badge badge2) {

        if (reviews.isEmpty()) {
            return NULL;
        }

        Map<Badge, Integer> badgeMap = new EnumMap<>(Badge.class);

        reviews.stream()
            .map(badgeFunction)
            .filter(badge ->
                badge != Badge.NULL
                    && (badge1.equals(badge)
                    || badge2.equals(badge)))
            .forEach(badge -> badgeMap.merge(badge, INCREMENT_VALUE, Integer::sum));

        int badge1Count = badgeMap.getOrDefault(badge1, DEFAULT_VALUE);
        int badge2Count = badgeMap.getOrDefault(badge2, DEFAULT_VALUE);

        if (badge1Count == DEFAULT_VALUE && badge2Count == DEFAULT_VALUE) {
            return NULL;
        }

        return badge1Count >= badge2Count ? badge1.name() : badge2.name();
    }
}
