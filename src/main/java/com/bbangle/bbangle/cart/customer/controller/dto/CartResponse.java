package com.bbangle.bbangle.cart.customer.controller.dto;

import com.bbangle.bbangle.board.domain.DiscountType;
import com.bbangle.bbangle.board.domain.SaleStatus;
import java.util.List;
import lombok.Builder;

public class CartResponse {

    @Builder
    public record CartListResponse(
        List<CartStoreDTO> carts
    ) {
        @Builder
        public record CartStoreDTO(
            Long storeId,
            String storeName,
            String storeProfile,
            List<CartItemDTO> items
        ) {}

        @Builder
        public record CartItemDTO(
            Long cartItemId,
            Long itemId,
            String itemName,
            String itemImg,
            SaleStatus status,
            String request, // TODO : CartItem.request 제거 시 DTO 필드도 제거
            PriceDTO price,
            List<AvailableOptionDTO> availableOptions,
            List<SelectedOptionDTO> selectedOptions
        ) {
            @Builder
            public record PriceDTO(
                Integer base,
                DiscountType discountType,
                Integer discountValue,
                Integer deliveryFee
            ) {}
        }

        @Builder
        public record AvailableOptionDTO(
            Long optionId,
            String optionName,
            Integer addedPrice,
            Integer stock
        ) {}

        @Builder
        public record SelectedOptionDTO(
            Long cartOptionId,
            Long optionId,
            String optionName,
            Integer addedPrice,
            Integer quantity
        ) {}
    }
}
