package com.bbangle.bbangle.store.dto;

import lombok.Builder;

@Builder
public record StoreDetailBoardDto(
    Long storeId,
    Long boardId,
    String boardProfile,
    String boardTitle,
    Integer boardPrice,
    Boolean isWished
) {

    public static StoreDetailBoardDto of(BoardsInStoreDto boardsInStoreDto) {
        return StoreDetailBoardDto.builder()
            .boardId(boardsInStoreDto.getBoardId())
            .boardTitle(boardsInStoreDto.getBoardTitle())
            .boardProfile(boardsInStoreDto.getBoardProfile())
            .boardPrice(boardsInStoreDto.getBoardPrice())
            .isWished(boardsInStoreDto.getIsWished())
            .build();
    }
}