package com.bbangle.bbangle.board.service.dto;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.SimilarityTypeEnum;
import com.bbangle.bbangle.board.service.solution.dao.CategoryDao;
import com.bbangle.bbangle.board.service.solution.dao.SoldOutDao;
import com.bbangle.bbangle.board.service.solution.dao.TagsDao;
import com.querydsl.core.annotations.QueryProjection;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class SimilarityBoardTestDto {

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
    SoldOutDao soldOutDao;
    CategoryDao categoryDao;
    Boolean isWished;
    SimilarityTypeEnum similarityType;

    @QueryProjection
    public SimilarityBoardTestDto(
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
        SimilarityTypeEnum similarityType,
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
        this.soldOutDao = SoldOutDao.builder()
            .soldOut(isSoldOut)
            .build();
        this.categoryDao = CategoryDao.builder()
            .category(category)
            .build();
        this.similarityType = similarityType;
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
