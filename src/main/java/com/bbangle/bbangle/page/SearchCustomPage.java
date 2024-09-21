package com.bbangle.bbangle.page;

import com.bbangle.bbangle.search.dto.response.SearchResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class SearchCustomPage<T> extends CustomPage<T> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long boardCount;

    public SearchCustomPage(
        T content,
        Long requestCursor,
        Boolean hasNext,
        Long boardCount
    ) {
        super(content, requestCursor, hasNext);
        this.boardCount = boardCount;
    }

    public SearchCustomPage(
        T content,
        Long requestCursor,
        Boolean hasNext
    ) {
        super(content, requestCursor, hasNext);
    }

    public static SearchCustomPage<SearchResponse> emptyPage() {
        long emptyResultNextCursor = -1L;
        boolean emptyResultHasNext = false;

        return from(
            SearchResponse.empty(),
            emptyResultNextCursor,
            emptyResultHasNext
        );
    }

    public static SearchCustomPage<SearchResponse> from(
        SearchResponse boardList,
        Long nextCursor,
        boolean hasNext
    ) {
        return new SearchCustomPage<>(boardList, nextCursor, hasNext);
    }

    public static SearchCustomPage<SearchResponse> from(
        SearchResponse boardList,
        Long requestCursor,
        boolean hasNext,
        long boardCnt
    ) {
        return new SearchCustomPage<>(boardList, requestCursor, hasNext, boardCnt);
    }

}
