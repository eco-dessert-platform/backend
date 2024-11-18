package com.bbangle.bbangle.board.domain;

import lombok.Builder;

@Builder
public class ViewCount {
    private static final String VIEW = "VIEW";
    Long boardId;
    String ipAddress;

    @Override
    public String toString() {
        return String.format("%s:%d:%s", VIEW, boardId, ipAddress);
    }
}
