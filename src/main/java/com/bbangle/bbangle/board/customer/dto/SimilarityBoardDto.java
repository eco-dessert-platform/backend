package com.bbangle.bbangle.board.customer.dto;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.repository.dao.TagsDao;
import com.querydsl.core.annotations.QueryProjection;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class SimilarityBoardDto {

    Long boardId;
    Long storeId;
    String thumbnail;
    String storeName;
    String title;
    Integer discountRate;
    Integer price;
    BigDecimal reviewRate;
    Long reviewCount;
    TagsDao tagsDao;
    Boolean isSoldOut;
    Category category;
    Boolean isWished;

    @QueryProjection
    public SimilarityBoardDto(
        Long boardId,
        Long storeId,
        String thumbnail,
        String storeName,
        String title,
        Integer discountRate,
        Integer price,
        BigDecimal reviewRate,
        Long reviewCount,
        Boolean glutenFreeTag,
        Boolean highProteinTag,
        Boolean sugarFreeTag,
        Boolean veganTag,
        Boolean ketogenicTag,
        Boolean isSoldOut,
        Category category,
        Boolean isWished
    ) {
        this.boardId = boardId;
        this.storeId = storeId;
        this.thumbnail = thumbnail;
        this.storeName = storeName;
        this.title = title;
        this.discountRate = discountRate;
        this.price = price;
        this.reviewRate = reviewRate;
        this.reviewCount = reviewCount;
        this.isSoldOut = isSoldOut;
        this.category = category;
        this.isWished = isWished;
        this.tagsDao = TagsDao.builder()
            .glutenFreeTag(glutenFreeTag)
            .highProteinTag(highProteinTag)
            .sugarFreeTag(sugarFreeTag)
            .veganTag(veganTag)
            .ketogenicTag(ketogenicTag)
            .build();
    }
}
