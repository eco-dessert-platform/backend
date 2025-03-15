package com.bbangle.bbangle.common.page;

import com.bbangle.bbangle.review.dto.ReviewInfoResponse;

import java.util.List;

public class ReviewCustomPage<T> extends CustomPage<T> {
    public ReviewCustomPage(T content, Long requestCursor, Boolean hasNext) {
        super(content, requestCursor, hasNext);
    }

    public static ReviewCustomPage<List<ReviewInfoResponse>> from(
            List<ReviewInfoResponse> reviewInfoListResponse,
            Long requestCursor,
            Boolean hasNext
    ) {
        return new ReviewCustomPage<>(reviewInfoListResponse, requestCursor, hasNext);
    }
}
