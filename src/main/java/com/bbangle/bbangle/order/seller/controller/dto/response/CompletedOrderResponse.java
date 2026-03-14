package com.bbangle.bbangle.order.seller.controller.dto.response;

import static com.bbangle.bbangle.order.domain.model.CompletedOrderStatus.CANCELED;
import static com.bbangle.bbangle.order.domain.model.CompletedOrderStatus.PURCHASED;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.order.domain.model.CompletedOrderStatus;
import com.bbangle.bbangle.order.domain.model.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public class CompletedOrderResponse {

    @Schema(description = "완료 주문 내역 조회 응답 (페이지 + 상태별 카운트)")
    public record CompletedOrderPageResponse(
        @Schema(description = "주문 목록 페이지")
        BbanglePageResponse<OrderSummary> orders,

        @Schema(description = "주문 상태별 카운트")
        CompletedOrderStatusCounts statusCounts
    ) {

    }

    @Schema(description = "완료 주문 상태별 카운트")
    public record CompletedOrderStatusCounts(
        @Schema(description = "전체", example = "13")
        long total,

        @Schema(description = "완료(구매확정)", example = "10")
        long purchased,

        @Schema(description = "취소", example = "2")
        long canceled,

        @Schema(description = "반품", example = "1")
        long returned,

        @Schema(description = "교환", example = "0")
        long exchanged
    ) {
        /**
         * 각 상태별 카운트를 받아 전체 합계(total)를 자동 계산합니다.
         * 전체(ALL) 탭과 개별 탭 카운트를 한 번에 처리하기 위해 total을 내부에서 집계합니다.
         */
        public static CompletedOrderStatusCounts of(
            long purchased,
            long canceled,
            long returned,
            long exchanged
        ) {
            long total = purchased + canceled + returned + exchanged;
            return new CompletedOrderStatusCounts(total, purchased, canceled, returned, exchanged);
        }
    }

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

        /**
         * Swagger UI에서 응답 예시로 표시할 샘플 데이터를 생성합니다.
         * 중첩 구조가 복잡하여 @Schema example 어노테이션으로 표현하기 어렵기 때문에
         * 메서드 형태로 제공합니다. SellerOrderApi 인터페이스의 @Operation에서 참조됩니다.
         */
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
