package com.bbangle.bbangle.board.customer.service.dto;

import com.bbangle.bbangle.common.mapstructure.Default;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreInfo {

        public record AllBoard(
                 Long boardId,
                 String thumbnail,
                 String title,
                 Integer price,
                 Integer discountRate,
                 BigDecimal reviewRate,
                 Long reviewCount,
                 Boolean isSoldOut,
                 Boolean isBbangcketing,
                 List<String> tags,
                 Boolean isBundled,
                 Boolean isWished
        ) {
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
