package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.domain.Board;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Getter
public final class BoardResponseDto {

    private final Long boardId;
    private final Long storeId;
    private final String storeName;
    private final String thumbnail;
    private final String title;
    private final int price;
    private Boolean isWished;
    private final Boolean isBundled;
    private final List<String> tags;

    @Builder
    public BoardResponseDto(
        Long boardId,
        Long storeId,
        String storeName,
        String thumbnail,
        String title,
        int price,
        Boolean isWished,
        Boolean isBundled,
        List<String> tags
    ) {
        this.boardId = boardId;
        this.storeId = storeId;
        this.storeName = storeName;
        this.thumbnail = thumbnail;
        this.title = title;
        this.price = price;
        this.isWished = isWished;
        this.isBundled = isBundled;
        this.tags = tags;
    }

    public static BoardResponseDto from(Board board, List<String> tags) {
        boolean isBundled = isBundled(board);

        return BoardResponseDto.builder()
            .boardId(board.getId())
            .storeId(board.getStore()
                .getId())
            .storeName(board.getStore()
                .getName())
            .thumbnail(board.getProfile())
            .title(board.getTitle())
            .price(board.getPrice())
            .isWished(false)
            .isBundled(isBundled)
            .tags(tags)
            .build();
    }

    private static boolean isBundled(Board board) {
        return !Objects.isNull(board.getProductList()) && board.getProductList()
            .size() != 1;
    }

    public static BoardResponseDto inFolder(BoardResponseDao board, boolean isBundled, List<String> tags) {
        return BoardResponseDto.builder()
            .boardId(board.boardId())
            .storeId(board.storeId())
            .storeName(board.storeName())
            .thumbnail(board.thumbnail())
            .title(board.title())
            .price(board.price())
            .isWished(true)
            .isBundled(isBundled)
            .tags(tags)
            .build();
    }

    public void updateLike(boolean status) {
        this.isWished = status;
    }

}
