package com.bbangle.bbangle.review.dto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import software.amazon.ion.Decimal;

@Getter
@Builder
@ToString
public class SummarizedReviewResponse {

    private BigDecimal rating;
    private Integer count;
    private List<String> badges;

    public static SummarizedReviewResponse of(
        BigDecimal rating,
        Integer count,
        List<String> badges) {

        return SummarizedReviewResponse.builder()
            .rating(rating)
            .count(count)
            .badges(badges)
            .build();
    }

    public static SummarizedReviewResponse getEmpty() {
        return SummarizedReviewResponse.builder()
            .rating(Decimal.valueOf(0))
            .count(0)
            .badges(Collections.emptyList())
            .build();
    }
}
