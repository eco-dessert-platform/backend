package com.bbangle.bbangle.board.seller.service.command;

import com.bbangle.bbangle.board.domain.BoardDetail;

public record BoardDetailCommand(
    String content
) {

    public BoardDetail toBoardDetail() {
        return BoardDetail.builder()
            .content(content)
            .build();
    }
}
