package com.bbangle.bbangle.board.customer.dto;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.repository.dao.TagsDao;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;

@Getter
public class BoardInfo {

    private Long boardId;
    private Long storeId;
    private String thumbnail;
    private String storeName;
    private String title;
    private Integer discountRate;
    private Integer price;
    private BigDecimal reviewRate;
    private Long reviewCount;
    private Boolean isWished;

    private List<TagsDao> tags;
    private Set<Category> categories;
    private Set<Boolean> isSoldOut;

    public BoardInfo(SimilarityBoardDto dto) {
        this.boardId = dto.getBoardId();
        this.storeId = dto.getStoreId();
        this.thumbnail = dto.getThumbnail();
        this.storeName = dto.getStoreName();
        this.title = dto.getTitle();
        this.discountRate = dto.getDiscountRate();
        this.price = dto.getPrice();
        this.reviewRate = dto.getReviewRate();
        this.reviewCount = dto.getReviewCount();
        this.isWished = dto.getIsWished();

        this.tags = new ArrayList<>();
        this.categories = new HashSet<>();
        this.isSoldOut = new HashSet<>();
    }

    public void addTag(TagsDao tag) {
        this.tags.add(tag);
    }

    public void addCategory(Category category) {
        this.categories.add(category);
    }

    public void addIsSoldOut(Boolean isSoldOut) {
        this.isSoldOut.add(isSoldOut);
    }
}

