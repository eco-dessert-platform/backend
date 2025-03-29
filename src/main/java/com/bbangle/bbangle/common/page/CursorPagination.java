package com.bbangle.bbangle.common.page;

import java.util.List;
import java.util.function.ToLongFunction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@RequiredArgsConstructor
public class CursorPagination<T> {

    private final List<T> content;
    private final Long nextCursor;
    private final Boolean hasNext;
    private final Long totalCount;

    /**
     * 요청 성공 시, 응답 dto 객체를 파라미터로 받음
     */
    public static <T> CursorPagination<T> of(List<T> data, int pageSize, Long totalCount, ToLongFunction<T> idExtractor) {
        boolean hasNext = data.size() > pageSize;
        Long nextCursor = -1L;

        int lastIndex = pageSize;
        if (hasNext) {
            T lastReponse = data.get(lastIndex);
            nextCursor = idExtractor.applyAsLong(lastReponse);
            data = data.subList(0, pageSize);
        }

        return new CursorPagination<>(data, nextCursor, hasNext, totalCount);
    }
}
