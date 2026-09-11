package com.bbangle.bbangle.board.seller.controller.dto.response;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.DeliveryType;
import com.bbangle.bbangle.board.domain.DiscountType;
import com.bbangle.bbangle.board.domain.InventoryStatus;
import com.bbangle.bbangle.board.domain.ProductionStartTime;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.seller.service.info.SellerBoardItemInfo;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.Builder;

public class SellerBoardResponse {

    public record SellerBoardListResponse(
        Map<SaleStatus, Long> tabCounts,
        BbanglePageResponse<SellerBoardSearchResponse> boards
    ) {

    }

    public record SellerBoardSearchResponse(
        Long boardId,
        String thumbnailUrl,
        String title,
        InventoryStatus inventoryStatus,
        int price,
        int discountPrice,
        int discountValue,
        Integer deliveryFee,
        Integer freeShippingConditions,
        DeliveryType deliveryType,
        SaleStatus saleStatus
    ) {

        public static SellerBoardSearchResponse from(SellerBoardItemInfo info) {
            return new SellerBoardSearchResponse(
                info.boardId(),
                info.thumbnailUrl(),
                info.title(),
                info.inventoryStatus(),
                info.price(),
                info.discountPrice(),
                info.discountValue(),
                info.deliveryFee(),
                info.freeShippingConditions(),
                info.deliveryType(),
                info.saleStatus()
            );
        }
    }

    @Builder
    @Schema(description = "상품 단건 상세 조회 DTO")
    public record SellerBoardDetailResponse(
        BoardDetailDTO boardDetailDTO,
        DeliveryDTO deliveryDTO,
        BoardImgDTO boardImgDTO,
        List<ProductOptionDTO> options,
        BoardContentDTO boardContent,
        ProductInfoNoticeDTO productInfoNotice
    ) {
        // Board 엔티티를 통해 조회 가능
        @Builder
        @Schema(description = "상품 기본 정보 DTO")
        public record BoardDetailDTO(
            @Schema(description = "상품 게시글 id", example = "1")
            Long boardId,   // Board.id
            @Schema(description = "상품명 (게시글 제목)", example = "글루텐 프리 케이크")
            String name,    // Board.title
            @Schema(description = "신선 식품 여부", example = "false")
            Boolean isFresh,    // Board.isFresh
            @Schema(description = "상품 제작 시작 시간", example = "T_03_04")
            ProductionStartTime productionStartTime,    // Board.productionStartTime
            PriceDTO price
        ) {
            @Builder
            @Schema(description = "상품 가격 정보 DTO")
            public record PriceDTO(
                @Schema(description = "상품 원가", example = "10000")
                Integer base,   // Board.price
                @Schema(description = "상품 할인 종류", example = "RATE")
                DiscountType discountType,  // Board.discountType
                @Schema(description = "상품 할인 값", example = "20")
                Integer discountValue// DiscountType.AMOUNT -> Board.discountValue 또는 DiscountType.Rate -> Board.discountRate
            ) {}
        }

        // Board 엔티티를 통해 조회 가능
        @Builder
        @Schema(description = "상품 배송 정보 DTO")
        public record DeliveryDTO(
            @Schema(description = "배송 조건", example = "유료/무료")
            String deliveryCondition,   // Board.deliveryCondition
            @Schema(description = "택배사", example = "CJ대한통운")
            String courier, // Board.courier
            @Schema(description = "배송비", example = "3000")
            Integer deliveryFee,    // Board.deliveryFee
            @Schema(description = "무료 배송 최소 금액", example = "50000")
            Integer freeShippingConditions  // Board.freeShippingConditions
        ) {}

        // Board.productImgs를 통해 조회 가능 (ProductImg 엔티티와 양방향, ProductImg가 FK를 가지고 있음)
        @Builder
        @Schema(description = "상품 이미지 DTO")
        public record BoardImgDTO(
            @Schema(description = "상품 대표 이미지", example = "item.png")
            String thumbnailImg,    // ProductImg.imgOrder이 ProductImg.THUMBNAIL_ORDER인 ProductImg.url
            @Schema(description = "상품 추가 이미지", example = "[item_1.png, item_2.png]")
            List<String> additionalImgs  // ProductImg.imgOrder이 ProductImg.THUMBNAIL_ORDER가 아닌 나머지 ProductImg.url
        ) {}

        // Board.products를 통해 조회 가능 (Product 엔티티와 양방향, Product가 FK를 가지고 있음)
        @Builder
        @Schema(description = "상품 옵션 정보 DTO")
        public record ProductOptionDTO(
            @Schema(description = "상품 옵션 Id", example = "1")
            Long optionId,  // Product.id
            @Schema(description = "상품 옵션 카테고리", example = "BREAD")
            Category category,  // Product.category
            @Schema(description = "상품 옵션 명", example = "저당 초콜릿 추가")
            String optionName,  // Product.title
            @Schema(description = "상품 옵션 성분 카테고리", example = "[GLUTEN_FREE, HIGH_PROTEIN]")
            List<TagEnum> tags,  // Product.getTags() 사용
            @Schema(description = "상품 옵션 추가 금액", example = "2000")
            Integer addedPrice,  // Product.price
            @Schema(description = "해당 옵션이 포함된 상품의 재고", example = "30")
            Integer stock,  // Product.stock
            WeekDTO week,
            NutritionDTO nutrition
        ) {
            @Builder
            @Schema(description = "상품 발송 요일")
            public record WeekDTO(
                Boolean monday,     // Product.monday
                Boolean tuesday,    // Product.tuesday
                Boolean wednesday,  // Product.wednesday
                Boolean thursday,   // Product.thursday
                Boolean friday,     // Product.friday
                Boolean saturday,   // Product.saturday
                Boolean sunday      // Product.sunday
            ) {}

            // Product.nutrition을 통해 접근 가능 (Nutrition은 엔티티가 아니라 VO 객체, Product 엔티티에 Embedded 되어있음)
            @Builder
            @Schema(description = "상품 영양 정보")
            public record NutritionDTO(
                @Schema(description = "총 중량 (g)", example = "150")
                Integer weight,         // Nutrition.weight
                @Schema(description = "1회 제공량 (g)", example = "150")
                Integer servingWeight,  // Nutrition.servingWeight
                @Schema(description = "탄수화물 (g)", example = "150")
                Integer carbohydrates,  // Nutrition.carbohydrates
                @Schema(description = "당류 (g)", example = "150")
                Integer sugars,         // Nutrition.sugars
                @Schema(description = "단백질 (g)", example = "150")
                Integer protein,        // Nutrition.protein
                @Schema(description = "지방 (g)", example = "150")
                Integer fat,            // Nutrition.fat
                @Schema(description = "칼로리 (kcal)", example = "150")
                Integer calories        // Nutrition.calories
            ) {}
        }

        // Board.boardDetail을 통해 접근 가능 (BoardDetail 엔티티와 양방향, BoardDetail이 FK를 가지고 있음)
        @Builder
        @Schema(description = "상세 페이지")
        public record BoardContentDTO(
            @Schema(description = "상세 페이지 내용", example = "<p>따끈한 빵그리 식빵</p>")
            String content      // BoardDetail.content
        ) {}

        // Board.productInfoNotice을 통해 접근 가능 (ProductInfoNotice 엔티티의 id를 Board가 FK로 가지고 있음)
        @Builder
        @Schema(description = "상품 정보 제공 고시")
        public record ProductInfoNoticeDTO(
            @Schema(description = "상품명", example = "빵그리의 식빵")
            String productName,     // ProductInfoNotice.productName
            @Schema(description = "식품의 유형", example = "빵")
            String foodType,        // ProductInfoNotice.foodType
            @Schema(description = "생산자", example = "빵그리")
            String manufacturer,    // ProductInfoNotice.manufacturer
            @Schema(description = "소재지", example = "서울특별시 강남구 테헤란로 131")
            String originLocation,  // ProductInfoNotice.originLocation
            @Schema(description = "제조년월일", example = "주문 후 익일 제조")
            String manufactureDate, // ProductInfoNotice.manufactureDate
            @Schema(description = "소비기한 또는 품질 유지기한", example = "출고 후 냉동 2주")
            String expirationDate,  // ProductInfoNotice.expirationDate
            @Schema(description = "포장 단위 별 내용물의 용량(중량) 수량", example = "상세페이지 첨부")
            String storageGuide,    // ProductInfoNotice.storageGuide
            @Schema(description = "포장 단위별 수량", example = "상세 페이지 첨부")
            String packagingQuantityUnit,   // ProductInfoNotice.packagingQuantityUnit
            @Schema(description = "원재료명(농수산물의 원산지 표시 등에 관한 법률)", example = "상세페이지 첨부")
            String rawMaterialName,     // ProductInfoNotice.rawMaterialName
            @Schema(description = "영양 성분", example = "해당사항 없음")
            String nutritionInfo,       // ProductInfoNotice.nutritionInfo
            @Schema(description = "유전자 변경 식품에 해당하는 경우 표시", example = "해당사항 없음")
            String transgenic,      // ProductInfoNotice.transgenic
            @Schema(description = "소비자 안전을 위한 주의사항", example = "배송 즉시 냉장보관 해주세요.")
            String customerWarning,     // ProductInfoNotice.customerWarning
            @Schema(description = "수입 식품의 경우", example = "미국산")
            String importFood       // ProductInfoNotice.importFood
        ) {}
    }
}
