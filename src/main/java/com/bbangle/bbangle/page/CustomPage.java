package com.bbangle.bbangle.page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class CustomPage<T> {

    private T content;
    private Long nextCursor;
    private Boolean hasNext;

}
