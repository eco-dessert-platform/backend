package com.bbangle.bbangle.order.controller.dto.response;

import com.bbangle.bbangle.order.controller.dto.CompletedOrderStatus;
import com.bbangle.bbangle.order.controller.dto.DayOfWeek;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public class CompletedOrderResponse {

    @Schema(description = "완료 주문 내역")
    public record OrderSummary(
        @Schema(description = "주문 ID") Long orderId,
        @Schema(description = "상태") CompletedOrderStatus status,
        @Schema(description = "주문 번호") String orderNum,
        @Schema(description = "택배사") String deliveryCompany,
        @Schema(description = "운송장 번호") String trackingNumber,
        @Schema(description = "결제일") LocalDateTime paidAt,
        @Schema(description = "결제 요일") DayOfWeek paidDayOfWeek,
        @Schema(description = "수취인명") String recipient,
        @Schema(description = "상품명") String productName,
        @Schema(description = "판매 수량") Integer quantity
    ) {
    }

}
