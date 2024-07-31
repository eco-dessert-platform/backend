package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.domain.Board;
import java.math.MathContext;
import java.math.RoundingMode;
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
    private final Double reviewRate;
    private final Integer reviewCount;
    private final Boolean isBbangcketing;
    private final Boolean isSoldOut;
    private final Integer discountRate;

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
        List<String> tags,
        Double reviewRate,
        Integer reviewCount,
        Boolean isBbangcketing,
        Boolean isSoldOut,
        Integer discountRate
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
        this.reviewRate = reviewRate;
        this.reviewCount = reviewCount;
        this.isBbangcketing = isBbangcketing;
        this.isSoldOut = isSoldOut;
        this.discountRate = discountRate;
    }

    public static BoardResponseDto from(BoardResponseDao board, boolean isBundled, List<String> tags, Boolean isBbangcketing, Boolean isSoldOut) {
        return BoardResponseDto.builder()
            .boardId(board.boardId())
            .storeId(board.storeId())
            .reviewRate(board.reviewRate().round(new MathContext(2, RoundingMode.HALF_UP)).doubleValue())
            .reviewCount(board.reviewCount())
            .isBbangcketing(isBbangcketing)
            .isSoldOut(isSoldOut)
            .discountRate(board.discountRate())
            .storeName(board.storeName())
            .thumbnail(board.thumbnail())
            .title(board.title())
            .price(board.price())
            .isWished(false)
            .isBundled(isBundled)
            .tags(tags)
            .build();
    }


    public static BoardResponseDto inFolder(BoardResponseDao board, boolean isBundled, List<String> tags, Boolean isBbangcketing, Boolean isSoldOut) {
        return BoardResponseDto.builder()
            .boardId(board.boardId())
            .storeId(board.storeId())
            .storeName(board.storeName())
            .reviewRate(board.reviewRate().round(new MathContext(2, RoundingMode.HALF_UP)).doubleValue())
            .reviewCount(board.reviewCount())
            .isBbangcketing(isBbangcketing)
            .isSoldOut(isSoldOut)
            .discountRate(board.discountRate())
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
