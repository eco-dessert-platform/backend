package com.bbangle.bbangle.boardstatistic.update;

public record StatisticUpdate(
    UpdateType updateType,
    Long boardId
) {

    public static StatisticUpdate updateReview(Long boardId) {
        return new StatisticUpdate(UpdateType.REVIEW, boardId);
    }

    public static StatisticUpdate updateViewCount(Long boardId) {
        return new StatisticUpdate(UpdateType.VIEW_COUNT, boardId);
    }

    public static StatisticUpdate updateWishCount(Long boardId) {
        return new StatisticUpdate(UpdateType.WISH_COUNT, boardId);
    }

}
