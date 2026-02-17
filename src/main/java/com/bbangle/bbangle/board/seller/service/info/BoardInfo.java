package com.bbangle.bbangle.board.seller.service.info;

import com.bbangle.bbangle.board.domain.Board;
import java.time.LocalDateTime;

public record BoardInfo(
    Long boardId,
    String title,
    LocalDateTime createdAt
) {

    public static BoardInfo from(Board board) {
        return new BoardInfo(
            board.getId(),
            board.getTitle(),
            board.getCreatedAt()
        );
    }
}
