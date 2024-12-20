package com.bbangle.bbangle.board.domain;

import lombok.Builder;

@Builder
public class RecommendCursorRange {

    private static final int TAKE_ROWS_COUNT = 50;
    private Integer maxProductCount;
    private Integer nextCursor;

    public RecommendCursorRange(int maxProductCount, int nextCursor) {
        this.maxProductCount = maxProductCount;
        this.nextCursor = nextCursor;
    }

    public int getStartCursor() {
        return nextCursor + 1;
    }

    public int getEndCursor() {
        if (maxProductCount >= 0) {
            int calculatedCursor = nextCursor + TAKE_ROWS_COUNT;
            return calculatedCursor >= maxProductCount ? maxProductCount : calculatedCursor;
        }

        return TAKE_ROWS_COUNT;
    }

    public boolean isEnd() {
        return nextCursor.equals(maxProductCount);
    }
}
