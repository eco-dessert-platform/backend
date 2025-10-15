package com.bbangle.bbangle.board.repository.dao;

import com.bbangle.bbangle.board.domain.Category;
import com.querydsl.core.annotations.QueryProjection;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BoardThumbnailDao(
    Long boardId,
    Long storeId,
    String storeName,
    String thumbnail,
    String title,
    Integer price,
    Boolean isWished,
    Category category,
    TagsDao tagsDao,
    BigDecimal reviewRate,
    Long reviewCount,
    LocalDateTime orderStartDate,
    Boolean isSoldOut,
    Integer discountRate
) {
    @QueryProjection
    public BoardThumbnailDao(
        Long boardId,
        Long storeId,
        String storeName,
        String thumbnail,
        String title,
        Integer price,
        Boolean isWished,
        Category category,
        Boolean glutenFreeTag,
        Boolean highProteinTag,
        Boolean sugarFreeTag,
        Boolean veganTag,
        Boolean ketogenicTag,
        BigDecimal reviewRate,
        Long reviewCount,
        LocalDateTime orderStartDate,
        Boolean isSoldOut,
        Integer discountRate
    ) {
        this(boardId, storeId, storeName, thumbnail, title, price, isWished, category,
            new TagsDao(glutenFreeTag, highProteinTag, sugarFreeTag, veganTag, ketogenicTag),
            reviewRate, reviewCount, orderStartDate, isSoldOut, discountRate);
    }

}
