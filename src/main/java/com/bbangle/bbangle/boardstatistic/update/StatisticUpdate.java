package com.bbangle.bbangle.boardstatistic.update;

import java.math.BigDecimal;

public record StatisticUpdate(
    UpdateType updateType,
    Long boardId,
    BigDecimal score
) {

    public static StatisticUpdate updateReviewCount(Long boardId) {
        return new StatisticUpdate(UpdateType.REVIEW_COUNT, boardId, null);
    }

    public static StatisticUpdate updateReviewRate(Long boardId, BigDecimal reviewRate) {
        return new StatisticUpdate(UpdateType.REVIEW_RATE, boardId, reviewRate);
    }

    public static StatisticUpdate updateViewCount(Long boardId) {
        return new StatisticUpdate(UpdateType.VIEW_COUNT, boardId, null);
    }

    public static StatisticUpdate updateWishCount(Long boardId, Boolean isWished) {
        BigDecimal score = Boolean.TRUE.equals(isWished) ? BigDecimal.ONE : BigDecimal.valueOf(-1);
        return new StatisticUpdate(UpdateType.WISH_COUNT, boardId, score);
    }

}
