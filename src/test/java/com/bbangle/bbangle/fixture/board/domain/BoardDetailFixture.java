package com.bbangle.bbangle.fixture.board.domain;

import com.bbangle.bbangle.board.domain.BoardDetail;

public final class BoardDetailFixture {
    private void BoardDetailFixture() {
    }

    public static BoardDetail defaultBoardDetailWithBoard(String content) {
        return BoardDetail.builder()
            .content(content)
            .build();
    }
}
