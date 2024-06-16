package com.bbangle.bbangle.image.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ImageCategory {

    MEMBER_PROFILE("사용자 프로필 이미지"),
    STORE("가게 대표 이미지"),
    BOARD("게시글 대표 이미지"),
    BOARD_DETAIL("게시글 설명 이미지"),
    REVIEW("리뷰 이미지"),

    ;

    private final String description;

}
