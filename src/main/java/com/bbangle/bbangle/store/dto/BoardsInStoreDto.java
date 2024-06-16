package com.bbangle.bbangle.store.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.util.Objects;
import lombok.Getter;

@Getter
public class BoardsInStoreDto {

    private Long boardId;
    private String boardProfile;
    private String boardTitle;
    private Integer boardPrice;
    private Integer boardView;

    private Boolean isWished;

    @QueryProjection
    public BoardsInStoreDto(
        Long boardId,
        String boardProfile,
        String boardTitle,
        Integer boardPrice,
        Integer boardView,
        Long wishlistBoardId
    ) {
        this.boardId = boardId;
        this.boardProfile = boardProfile;
        this.boardTitle = boardTitle;
        this.boardPrice = boardPrice;
        this.boardView = boardView;
        this.isWished = isNonEmptyWishlist(wishlistBoardId);
    }

    private Boolean isNonEmptyWishlist(Long wishlistId) {
        return Objects.nonNull(wishlistId) && wishlistId > 0;
    }
}