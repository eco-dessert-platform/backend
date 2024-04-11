package com.bbangle.bbangle.page;

import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class CustomPage<T> {

    private T content;
    private final Long requestCursor;
    private final Boolean hasNext;

    public void updateContent(T content) {
        this.content = content;
    }

}
