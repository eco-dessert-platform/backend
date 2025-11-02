package com.bbangle.bbangle.board.customer.service.dto;

public class BoardDetailCommand {

    public record Main(
        Long boardId,
        Long memberId,
        String ipAddress
    ) {

    }

}
