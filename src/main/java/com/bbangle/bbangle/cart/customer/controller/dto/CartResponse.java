package com.bbangle.bbangle.cart.customer.controller.dto;

import com.bbangle.bbangle.board.domain.DiscountType;
import com.bbangle.bbangle.board.domain.SaleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

public class CartResponse {

    @Builder
    @Schema(description = "장바구니 목록 DTO")
    public record CartListResponse(
        List<CartStoreDTO> carts
    ) {
        @Builder
        @Schema(description = "스토어별 장바구니 상품 목록 DTO")
        public record CartStoreDTO(
            @Schema(description = "스토어 Id", example = "1")
            Long storeId,
            @Schema(description = "스토어 이름", example = "빵그리의 오븐")
            String storeName,
            @Schema(description = "스토어 프로필", example = "example.jpg")
            String storeProfile,
            List<CartItemDTO> items
        ) {}

        @Builder
        @Schema(description = "장바구니에 담긴 상품 DTO")
        public record CartItemDTO(
            @Schema(description = "장바구니에 담긴 상품 id (실제 상품 Id가 아닌 본인 계정에 담긴 상품 id입니다.", example = "1")
            Long cartItemId,
            @Schema(description = "원래 상품 id", example = "1")
            Long itemId,
            @Schema(description = "상품 이름", example = "글루텐 프리 케이크")
            String itemName,
            @Schema(description = "상품 대표 사진", example = "item.png")
            String itemImg,
            @Schema(description = "상품 판매 상태", example = "ON_SALE")
            SaleStatus status,
            PriceDTO price,
            List<AvailableOptionDTO> availableOptions,
            List<SelectedOptionDTO> selectedOptions
        ) {
            @Builder
            @Schema(description = "상품 가격 정보 DTO")
            public record PriceDTO(
                @Schema(description = "상품 원가", example = "10000")
                Integer base,
                @Schema(description = "상품 할인 종류", example = "RATE")
                DiscountType discountType,
                @Schema(description = "상품 할인 값", example = "20")
                Integer discountValue,
                @Schema(description = "배달료", example = "3000")
                Integer deliveryFee
            ) {}
        }

        @Builder
        @Schema(description = "변경가능한 상품 옵션 종류")
        public record AvailableOptionDTO(
            @Schema(description = "상품 옵션 Id", example = "1")
            Long optionId,
            @Schema(description = "상품 옵션 명", example = "저당 초콜릿 추가")
            String optionName,
            @Schema(description = "상품 옵션 추가 금액", example = "2000")
            Integer addedPrice,
            @Schema(description = "해당 옵션이 포함된 상품의 재고", example = "30")
            Integer stock
        ) {}

        @Builder
        @Schema(description = "장바구니에 담겨있는 상품 옵션 종류")
        public record SelectedOptionDTO(
            @Schema(description = "장바구니에 담긴 상품 옵션 Id", example = "1")
            Long cartOptionId,
            @Schema(description = "상품 옵션 Id", example = "1")
            Long optionId,
            @Schema(description = "상품 옵션 명", example = "저당 초콜릿 추가")
            String optionName,
            @Schema(description = "상품 옵션 추가 금액", example = "2000")
            Integer addedPrice,
            @Schema(description = "장바구니에 담긴 상품 옵션의 수량", example = "2")
            Integer quantity
        ) {}
    }
}
