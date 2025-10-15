package com.bbangle.bbangle.search.customer.service.dto;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.common.mapstructure.Default;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class SearchInfo {

    @Data
    public static class BoardsInfo {
        List<Board> boards;
        Long boardCount;
        int boardLimitSize;
        Map<Long, Boolean> boardWishedMap;

        @Default
        public BoardsInfo(
                List<Board> boards,
                Long boardCount
        ) {
            this.boards = boards;
            this.boardCount = boardCount;
        }
    }

    @Data
    @AllArgsConstructor
    public static class Select {
        private Long boardId;
        private Long storeId;
        private String storeName;
        private String thumbnail;
        private String title;
        private Integer price;
        private Boolean isBundled;
        private List<String> tags;
        private BigDecimal reviewRate;
        private Long reviewCount;
        private Boolean isBbangcketing;
        private Boolean isSoldOut;
        private Integer discountRate;
        private Boolean isWished;
    }

    @Data
    @AllArgsConstructor
    public static class SearchBoardPage {
        private List<SearchInfo.Select> boards;
        private Long allItemCount;
    }

    @Data
    @NoArgsConstructor
    public static class CursorCondition {
        private Long cursorId;
        private Double boardBasicScore;
        private Integer price;
        private Long boardWishedCount;
        private Long boardReviewCount;

        @QueryProjection
        public CursorCondition(Long cursorId, Double boardBasicScore, Integer price, Long boardWishedCount, Long boardReviewCount) {
            this.cursorId = cursorId;
            this.boardBasicScore = boardBasicScore;
            this.price = price;
            this.boardWishedCount = boardWishedCount;
            this.boardReviewCount = boardReviewCount;
        }

        public static CursorCondition empty() {
            return new CursorCondition(Long.MAX_VALUE, 0.0, 0, 0L, 0L);
        }

    }

}
