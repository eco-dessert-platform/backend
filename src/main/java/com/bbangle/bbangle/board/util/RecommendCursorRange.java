package com.bbangle.bbangle.board.util;

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
        return nextCursor == 0 ? nextCursor + 1 : nextCursor;
    }

    public int getEndCursor() {
        return nextCursor + TAKE_ROWS_COUNT >= maxProductCount ? maxProductCount : nextCursor;
    }
}
