package com.bbangle.bbangle.page;

import java.util.List;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 커서 기반 페이지네이션을 사용하여 처리된 데이터를 담는 응답 클래스입니다.
 *
 * @param <T> 페이지네이션 되는 개별 항목의 타입
 * @param <U> 변환 함수 적용 후 반환되는 처리된 데이터의 타입
 */
@Getter
@RequiredArgsConstructor
public class ProcessedDataCursorResponse<T, U> {

    private static final Long NO_NEXT_CURSOR = -1L;
    private static final boolean NO_HAS_NEXT = false;

    private final U data;
    private final Long nextCursor;
    private final Boolean hasNext;

    /**
     * 주어진 데이터 리스트에 페이지네이션을 적용하여 새로운 {@link ProcessedDataCursorResponse}를 생성합니다. 변환 함수를 적용하여 처리된
     * 데이터를 반환합니다.
     *
     * @param data        변환될 데이터 리스트
     * @param pageSize    페이지 크기 (한 페이지에 들어갈 항목의 개수)
     * @param idExtractor 항목에서 ID를 추출하는 함수 (커서로 사용)
     * @param func        데이터 리스트를 변환하여 처리된 데이터를 반환하는 함수
     * @return 처리된 데이터를 포함하는 {@link ProcessedDataCursorResponse} 객체
     */
    public static <T, U> ProcessedDataCursorResponse<T, U> of(
        List<T> data,
        int pageSize,
        ToLongFunction<T> idExtractor,
        Function<List<T>, U> func
    ) {
        boolean hasNext = data.size() > pageSize;
        Long nextCursor = -1L;

        if (hasNext) {
            T lastReponse = data.get(pageSize - 1);
            nextCursor = idExtractor.applyAsLong(lastReponse);
            data = data.subList(0, pageSize);
        }

        U processedData = func.apply(data);

        return new ProcessedDataCursorResponse<>(processedData, nextCursor, hasNext);
    }

    public static <T, U> ProcessedDataCursorResponse<T, U> empty(
        U processedData) {
        return new ProcessedDataCursorResponse(processedData, NO_NEXT_CURSOR, NO_HAS_NEXT);
    }
}