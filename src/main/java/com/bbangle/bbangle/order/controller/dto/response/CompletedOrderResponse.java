package com.bbangle.bbangle.order.controller.dto.response;

import static com.bbangle.bbangle.order.controller.dto.CompletedOrderStatus.CANCELED;
import static com.bbangle.bbangle.order.controller.dto.CompletedOrderStatus.PURCHASED;

import com.bbangle.bbangle.order.controller.dto.CompletedOrderStatus;
import com.bbangle.bbangle.order.controller.dto.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public class CompletedOrderResponse {

    @Schema(description = "완료 주문 내역")
    public record OrderSummary(
        @Schema(description = "주문 ID") Long orderId,
        @Schema(description = "주문 번호") String orderNum,
        @Schema(description = "결제일")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDateTime paidAt,
        @Schema(description = "결제 요일") DayOfWeek paidDayOfWeek,
        @Schema(description = "수취인명") String recipient,
        List<OrderItem> orderItems
    ) {

        public static OrderSummary sample() {
            OrderItem item1 = OrderItem.of(1L, PURCHASED, "CJ대한통운", "123-123", "저칼로리 베이글", 5);
            OrderItem item2 = OrderItem.of(2L, CANCELED, "롯데택배", "123-456", "저당 초콜릿", 10);
            return OrderSummary.of(
                1L,
                "000-123",
                LocalDateTime.of(2024, 1, 1, 12, 0),
                DayOfWeek.MONDAY,
                "홍길동",
                List.of(item1, item2)
            );
        }

        public static OrderSummary of(
            Long orderId,
            String orderNum,
            LocalDateTime paidAt,
            DayOfWeek paidDayOfWeek,
            String recipient,
            List<OrderItem> orderItems
        ) {
            return new OrderSummary(orderId, orderNum, paidAt, paidDayOfWeek, recipient,
                orderItems);
        }

        public record OrderItem(
            @Schema(description = "주문상품ID") Long orderItemId,
            @Schema(description = "상태") CompletedOrderStatus status,
            @Schema(description = "택배사") String deliveryCompany,
            @Schema(description = "운송장 번호") String trackingNumber,
            @Schema(description = "상품명") String productName,
            @Schema(description = "판매 수량") Integer quantity
        ) {

            public static OrderItem of(
                Long orderItemId,
                CompletedOrderStatus status,
                String deliveryCompany,
                String trackingNumber,
                String productName,
                Integer quantity
            ) {
                return new OrderItem(orderItemId, status, deliveryCompany, trackingNumber,
                    productName, quantity);
            }
        }
    }

}
