package com.bbangle.bbangle.board.customer.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.util.Objects;
import lombok.Getter;

@Getter
public class TitleDto {

    Long boardId;
    String title;

    @QueryProjection
    public TitleDto(Long boardId, String title) {
        this.boardId = boardId;
        this.title = title;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        TitleDto titleDto = (TitleDto) object;
        return this.boardId == titleDto.boardId && Objects.equals(this.title, titleDto.title);
    }
}
