package com.bbangle.bbangle.review.customer.dto;

import com.bbangle.bbangle.review.domain.Badge;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record ReviewRateResponse(
    BigDecimal rating,
    Integer count,
    TasteDto taste,
    BrixDto brix,
    TextureDto texture
) {

    public static ReviewRateResponse from(List<ReviewDto> reviews) {
        int good = 0;
        int bad = 0;
        int sweet = 0;
        int plain = 0;
        int soft = 0;
        int dry = 0;
        int count = 0;
        BigDecimal rateSum = BigDecimal.ZERO;
        if (reviews != null && !reviews.isEmpty()) {
            for (ReviewDto review : reviews) {
                rateSum = rateSum.add(review.getRate());
                if (review.getBadgeTaste().equals(Badge.GOOD)) {
                    good++;
                } else if (review.getBadgeTaste().equals(Badge.BAD)) {
                    bad++;
                }

                if (review.getBadgeBrix().equals(Badge.SWEET)) {
                    sweet++;
                } else if (review.getBadgeBrix().equals(Badge.PLAIN)) {
                    plain++;
                }

                if (review.getBadgeTexture().equals(Badge.SOFT)) {
                    soft++;
                } else if (review.getBadgeTexture().equals(Badge.DRY)) {
                    dry++;
                }
            }
            count = reviews.size();
            rateSum = rateSum.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP);
        }
        return ReviewRateResponse.builder()
            .taste(new TasteDto(good, bad))
            .brix(new BrixDto(sweet, plain))
            .texture(new TextureDto(soft, dry))
            .rating(rateSum)
            .count(count)
            .build();
    }
}
