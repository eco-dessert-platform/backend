package com.bbangle.bbangle.board.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.util.Objects;

public record BoardAndDetailDto(
    Long boardId,
    String boardProfile,
    String boardTitle,
    Integer boardPrice,
    Boolean monday,
    Boolean tuesday,
    Boolean wednesday,
    Boolean thursday,
    Boolean friday,
    Boolean saturday,
    Boolean sunday,
    String purchaseUrl,
    Boolean status,
    Long boardDetailId,
    Integer order,
    String boardDetailUrl,
    Long wishListBoardId
) {

    @QueryProjection
    public BoardAndDetailDto {
    }

    public BoardDetailDto toBoardDetailDto() {
        return BoardDetailDto.builder()
            .boardDetailId(boardId)
            .boardDetailOrder(order)
            .boardDetailUrl(boardDetailUrl)
            .build();
    }

    public Boolean isNonEmptyWishlist() {
        return Objects.nonNull(wishListBoardId) && wishListBoardId > 0;
    }

}
