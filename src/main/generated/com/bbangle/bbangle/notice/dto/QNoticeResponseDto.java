package com.bbangle.bbangle.notice.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.bbangle.bbangle.notice.dto.QNoticeResponseDto is a Querydsl Projection type for NoticeResponseDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QNoticeResponseDto extends ConstructorExpression<NoticeResponseDto> {

    private static final long serialVersionUID = 1398468132L;

    public QNoticeResponseDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> createdAt) {
        super(NoticeResponseDto.class, new Class<?>[]{long.class, String.class, String.class}, id, title, createdAt);
    }

}

