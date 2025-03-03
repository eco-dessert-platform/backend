package com.bbangle.bbangle.search.dto;

import com.bbangle.bbangle.board.domain.Board;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class SearchBoardResponseDto {

    Long boardId;
    Long storeId;
    String storeName;
    String thumbnail;
    String title;
    Integer price;
    Boolean isBundled;
    List<String> tags;
    BigDecimal reviewRate;
    Long reviewCount;
    Boolean isBbangcketing;
    Boolean isSoldOut;
    Integer discountRate;
    Boolean isWished;

    public SearchBoardResponseDto(Board board) {
        this.boardId = board.getId();
        this.storeId = board.getStore().getId();
        this.storeName = board.getStore().getName();
        this.thumbnail = board.getProfile();
        this.title = board.getTitle();
        this.price = board.getPrice();
        this.isBundled = board.isBundled();
        this.tags = board.getTags();
        this.reviewRate = board.getBoardStatistic().getBoardReviewGrade();
        this.reviewCount = board.getBoardStatistic().getBoardReviewCount();
        this.isBbangcketing = board.isBbangketing();
        this.isSoldOut = board.isSoldOut();
        this.discountRate = board.getDiscountRate();
    }

    public void updateWished(Boolean isWished) {
        this.isWished = isWished;
    }

}
