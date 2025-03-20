package com.bbangle.bbangle.search.service.dto;

import com.bbangle.bbangle.search.dto.SearchBoardResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class SearchInfo {

    @Data
    @AllArgsConstructor
    public class Select {
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
    public static class SearchBoardPage{
        private List<SearchInfo.Select> boards;
        private Long allItemCount;
    }

}
