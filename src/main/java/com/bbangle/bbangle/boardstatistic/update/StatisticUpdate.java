package com.bbangle.bbangle.boardstatistic.update;

import java.math.BigDecimal;

public record StatisticUpdate (
    UpdateType updateType,
    Long boardId,
    BigDecimal score
){

    public static StatisticUpdate updateReviewCount(Long boardId){
        return new StatisticUpdate(UpdateType.REVIEW_COUNT, boardId, null);
    }

    public static StatisticUpdate updateReviewRate(Long boardId, BigDecimal reviewRate){
        return new StatisticUpdate(UpdateType.REVIEW_RATE, boardId, reviewRate);
    }

    public static StatisticUpdate updateViewCount(Long boardId){
        return new StatisticUpdate(UpdateType.VIEW_COUNT, boardId, null);
    }

    public static StatisticUpdate updateLikeCount(Long boardId){
        return new StatisticUpdate(UpdateType.LIKE_COUNT, boardId, null);
    }

}
