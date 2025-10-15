package com.bbangle.bbangle.board.customer.dto;

import com.bbangle.bbangle.push.domain.PushType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BoardDetailResponse {

    @Data
    public static class Main {
        private Store store;
        private Board board;
        List<Product> products;
    }

    @Data
    public static class Store {
        private Long storeId;
        private String storeTitle;
        private String storeProfile;
        private Boolean isStoreWished;
    }

    @Data
    public static class Board {
        private Long boardId;
        private String boardProfile;
        private String boardTitle;
        private Integer boardPrice;
        private String purchaseUrl;
        private Boolean isSoldout;
        private Integer deliveryFee;
        private Integer freeShippingConditions;
        private int discountRate;
        private Boolean isBoardWished;
        private Boolean isBundled;
        private List<String> boardImages;
        private String boardDetail;
    }

    @Data
    public static class Product {
        private Long id;
        private String title;
        private Boolean glutenFreeTag;
        private Boolean highProteinTag;
        private Boolean sugarFreeTag;
        private Boolean veganTag;
        private Boolean ketogenicTag;
        private Integer price;
        private Nutrient nutrient;
        private Boolean isSoldout;
        private ProductOrderType orderType;
        private Boolean isBbangketting;
    }

    @Data
    public static class ProductOrderType {
        private PushType orderType;
        private boolean monday;
        private boolean tuesday;
        private boolean wednesday;
        private boolean thursday;
        private boolean friday;
        private boolean saturday;
        private boolean sunday;
        private LocalDateTime orderStartDate;
        private LocalDateTime orderEndDate;
    }

    @Data
    public static class SimilarityBoard {
        private Long boardId;
        private Long storeId;
        private String thumbnail;
        private String storeName;
        private String title;
        private Integer discountRate;
        private Integer price;
        private BigDecimal reviewRate;
        private Long reviewCount;
        private List<String> tags;
        private Boolean isWished;
        private Boolean isBundled;
        private Boolean isSoldOut;
    }

}
