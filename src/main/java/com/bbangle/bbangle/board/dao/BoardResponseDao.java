package com.bbangle.bbangle.board.dao;

import com.bbangle.bbangle.board.domain.Category;
import com.querydsl.core.annotations.QueryProjection;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BoardResponseDao(
    Long boardId,
    Long storeId,
    String storeName,
    String thumbnail,
    String title,
    Integer price,
    Category category,
    TagsDao tagsDao,
    BigDecimal reviewRate,
    Long reviewCount,
    LocalDateTime orderStartDate,
    Boolean isSoldOut,
    Integer discountRate
) {
    @QueryProjection
    public BoardResponseDao(
        Long boardId,
        Long storeId,
        String storeName,
        String thumbnail,
        String title,
        Integer price,
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
        this(boardId, storeId, storeName, thumbnail, title, price, category,
            new TagsDao(glutenFreeTag, highProteinTag, sugarFreeTag, veganTag, ketogenicTag),
            reviewRate, reviewCount, orderStartDate, isSoldOut, discountRate);
    }

}
