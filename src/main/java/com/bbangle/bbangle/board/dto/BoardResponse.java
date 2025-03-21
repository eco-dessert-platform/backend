package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.dao.BoardThumbnailDao;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BoardResponse {

    private Long boardId;
    private Long storeId;
    private String storeName;
    private String thumbnail;
    private String title;
    private int price;
    private Boolean isWished;
    private Boolean isBundled;
    private List<String> tags;
    private Double reviewRate;
    private Long reviewCount;
    private Boolean isBbangcketing;
    private Boolean isSoldOut;
    private Integer discountRate;

    @Builder
    public BoardResponse(
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
        Long reviewCount,
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

    public static BoardResponse from(BoardThumbnailDao board, boolean isBundled, List<String> tags, Boolean isBbangcketing, Boolean isSoldOut) {
        return BoardResponse.builder()
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


    public static BoardResponse inFolder(BoardThumbnailDao board, boolean isBundled, List<String> tags, Boolean isBbangcketing, Boolean isSoldOut) {
        return BoardResponse.builder()
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
