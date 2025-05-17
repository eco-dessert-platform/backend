package com.bbangle.bbangle.store.service.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreInfo {

        @Data
        public static class BestBoard {
                private Long boardId;
                private String thumbnail;
                private String title;
                private Integer price;
                private Integer discountRate;
                BigDecimal reviewRate;
                Long reviewCount;
                private Boolean isSoldOut;
                private Boolean isBbangketing;
                private List<String> tags;
                private Boolean isBundled;
                private Boolean isWished;
        }

}
