package com.bbangle.bbangle.store.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.util.Objects;
import lombok.Getter;

@Getter
public class PopularBoardDto {

    private Long boardId;
    private String boardProfile;
    private String boardTitle;
    private Integer boardPrice;
    private Boolean isWished;

    @QueryProjection
    public PopularBoardDto(
        Long boardId,
        String boardProfile,
        String boardTitle,
        Integer boardPrice,
        Long wishlistBoardId
    ) {
        this.boardId = boardId;
        this.boardProfile = boardProfile;
        this.boardTitle = boardTitle;
        this.boardPrice = boardPrice;
        this.isWished = isNonEmptyWishlist(wishlistBoardId);
    }

    private Boolean isNonEmptyWishlist(Long wishlistId) {
        return Objects.nonNull(wishlistId) && wishlistId > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PopularBoardDto that = (PopularBoardDto) o;
        return Objects.equals(boardId, that.boardId) &&
            Objects.equals(boardProfile, that.boardProfile) &&
            Objects.equals(boardTitle, that.boardTitle) &&
            Objects.equals(boardPrice, that.boardPrice) &&
            Objects.equals(isWished, that.isWished);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boardId, boardProfile, boardTitle, boardPrice, isWished);
    }

}