package com.bbangle.bbangle.linktracking.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LinkChannel {

    INSTAGRAM("인스타그램"),
    NAVER_BLOG("네이버 블로그");

    private final String description;
}
