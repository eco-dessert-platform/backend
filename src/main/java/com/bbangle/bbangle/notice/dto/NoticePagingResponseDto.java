package com.bbangle.bbangle.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@AllArgsConstructor
@Getter
public class NoticePagingResponseDto {

    private static final long LAST_PAGE = -1L;

    private List<NoticeResponseDto> contents;
    private long lastPage;
    private long nextPage;

    public static NoticePagingResponseDto of(Page<NoticeResponseDto> noticePaging) {
        if (!noticePaging.hasNext()) {
            return NoticePagingResponseDto.getLastPage(noticePaging.getContent(),
                noticePaging.getTotalPages() - 1);
        }
        return NoticePagingResponseDto.newPagingHasNext(noticePaging.getContent(),
            noticePaging.getTotalPages() - 1,
            noticePaging.getPageable()
                .getPageNumber() + 1);
    }

    private static NoticePagingResponseDto getLastPage(
        List<NoticeResponseDto> noticePaging,
        long lastPage
    ) {
        return newPagingHasNext(noticePaging, lastPage, LAST_PAGE);
    }

    private static NoticePagingResponseDto newPagingHasNext(
        List<NoticeResponseDto> noticePaging,
        long lastPage,
        long nextPage
    ) {
        return new NoticePagingResponseDto(noticePaging, lastPage, nextPage);
    }

}
