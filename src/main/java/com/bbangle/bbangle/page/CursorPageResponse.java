package com.bbangle.bbangle.page;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.ToLongFunction;

@Getter
@RequiredArgsConstructor
public class CursorPageResponse<T> {

    private final List<T> data;
    private final Long nextCursor;
    private final Boolean hasNext;

    /**
     * 요청 성공 시, 응답 dto 객체를 파라미터로 받음
     */
    public static <T> CursorPageResponse<T> of(List<T> data, int pageSize, ToLongFunction<T> idExtractor) {
        boolean hasNext = data.size() > pageSize;
        Long nextCursor = -1L;

        if (hasNext) {
            T lastReponse = data.get(pageSize - 1);
            nextCursor = idExtractor.applyAsLong(lastReponse);
            data = data.subList(0, pageSize);
        }

        return new CursorPageResponse<>(data, nextCursor, hasNext);
    }
}