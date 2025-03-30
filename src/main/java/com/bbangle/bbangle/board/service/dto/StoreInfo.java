package com.bbangle.bbangle.board.service.dto;

import com.bbangle.bbangle.common.mapstructure.Default;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreInfo {

        @Data
        public static class AllBoard {

                private Long boardId;
                private String thumbnail;
                private String title;
                private Integer price;
                private Integer discountRate;
                private BigDecimal reviewRate;
                private Long reviewCount;
                private Boolean isSoldOut;
                private Boolean isBbangcketing;
                private List<String> tags;
                private Boolean isBundled;
                private Boolean isWished;

                @Default
                public AllBoard(Long boardId, String thumbnail, String title, Integer price,
                    Integer discountRate,
                    BigDecimal reviewRate, Long reviewCount, Boolean isSoldOut,
                    Boolean isBbangcketing,
                    List<String> tags, Boolean isBundled) {
                        this.boardId = boardId;
                        this.thumbnail = thumbnail;
                        this.title = title;
                        this.price = price;
                        this.discountRate = discountRate;
                        this.reviewRate = reviewRate;
                        this.reviewCount = reviewCount;
                        this.isSoldOut = isSoldOut;
                        this.isBbangcketing = isBbangcketing;
                        this.tags = tags;
                        this.isBundled = isBundled;
                }

                public void updateWished(Boolean isWished) {
                        this.isWished = isWished;
                }
        }

        @Data
        public static class Store {

                private Long id;
                private String title;
                private String profile;
                private Boolean isWished;

                @Default
                public Store(
                    Long id,
                    String title,
                    String profile
                ) {
                        this.id = id;
                        this.title = title;
                        this.profile = profile;
                }

                public void updateWished(Boolean isWished) {
                        this.isWished = isWished;
                }
        }

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

        @Getter
        public static class StoreDetail {

                private Long storeId;
                private String profile;
                private String storeName;
                private String introduce;
                private Boolean isWished;

                @Default
                public StoreDetail(Long storeId, String profile, String storeName,
                    String introduce) {
                        this.storeId = storeId;
                        this.profile = profile;
                        this.storeName = storeName;
                        this.introduce = introduce;
                }

                public void updateWished(Boolean isWished) {
                        this.isWished = isWished;
                }
        }

}
