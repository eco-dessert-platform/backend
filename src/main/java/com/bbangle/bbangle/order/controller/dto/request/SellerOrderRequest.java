package com.bbangle.bbangle.order.controller.dto.request;

import com.bbangle.bbangle.order.service.model.SellerOrderCommand.OrderConfirmCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class SellerOrderRequest {

    @Schema(description = "판매자 주문상품 발주 확인 요청 DTO")
    public record OrderConfirmRequest(

        @Schema(description = "발주 확인 대상 주문상품 ID 목록")
        @NotEmpty(message = "orderItemIds는 필수입니다.")
        List<Long> orderItemIds

    ) {

        public OrderConfirmCommand toCommand(Long sellerId, Long orderId) {
            return OrderConfirmCommand.builder()
                .sellerId(sellerId)
                .orderId(orderId)
                .orderItemIds(orderItemIds)
                .build();
        }
    }
}
