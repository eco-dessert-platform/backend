package com.bbangle.bbangle.order.seller.controller.dto.request;

import com.bbangle.bbangle.common.dto.SearchFormDto;
import com.bbangle.bbangle.order.domain.model.CompletedOrderSearchType;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderSearchCommand;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

public class OrderRequest {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "orderSearchRequest",
        allOf = {SearchFormDto.DefaultSearchCondition.class})
    public static class OrderSearchRequest {

        @Schema(description = "배송상태")
        private OrderDeliveryStatus orderStatus;

        @Schema(description = "검색 상세조건")
        private CompletedOrderSearchType searchType;

        @NotNull
        @Valid
        @JsonUnwrapped
        @Schema(hidden = true, description = "기본 검색 조건")
        private SearchFormDto.DefaultSearchCondition defaultSearchCondition;

        /**
         * OrderSearchRequest를 OrderSearchCommand로 변환합니다.
         *
         * @param sellerId 판매자 ID
         * @return OrderSearchCommand
         */
        public OrderSearchCommand toCommand(Long sellerId, Pageable page) {
            return OrderSearchCommand.builder()
                .sellerId(sellerId)
                .orderDeliveryStatus(this.orderStatus)
                .searchType(this.searchType)
                .searchCondition(this.defaultSearchCondition)
                .page(page)
                .build();
        }

    }
}
