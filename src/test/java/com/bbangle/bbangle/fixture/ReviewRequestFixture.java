package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.review.domain.Badge;
import com.bbangle.bbangle.review.customer.dto.ReviewRequest;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.datafaker.Faker;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewRequestFixture {

    private static final Faker faker = new Faker();

    public static ReviewRequest createReviewRequest(Long boardId) {
        return new ReviewRequest(
            List.of(Badge.BAD, Badge.SWEET, Badge.SOFT),
            BigDecimal.valueOf(4.0),
            faker.movie().quote(),
            boardId,
            List.of(
                faker.internet().url(),
                faker.internet().url())
        );
    }

}
