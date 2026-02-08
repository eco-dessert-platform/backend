package com.bbangle.bbangle.order.seller.controller.dto.response;


import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderDelivery;
import com.bbangle.bbangle.order.seller.controller.model.PaymentInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

public class OrderResponse {

    @Builder
    public record OrderSearchResponse(
        @Schema(description = "주문번호")
        String orderNumber,

        @Schema(description = "수취인명")
        String recipientName,

        @Schema(description = "주문 품목 리스트")
        List<OrderItemListResponse.OrderItemList> orderItems,

        @Schema(description = "결제수단")
        PaymentInfo paymentInfo,

        @Schema(description = "총 주문금액")
        String totalOrderPrice,

        @Schema(description = "판매자 ID")
        Long sellerId

    ) {

        /**
         * Order 엔티티로부터 OrderSearchResponse를 생성하는 팩토리 메서드
         */
        public static OrderSearchResponse from(
            Order order,
            List<OrderItemListResponse.OrderItemList> orderItemDetails,
            PaymentInfo paymentInfo,
            OrderDelivery firstDelivery
        ) {
            if (order.getOrderItems().isEmpty()) {
                throw new IllegalArgumentException("Order must have at least one OrderItem");
            }

            return OrderSearchResponse.builder()
                .orderNumber(order.getOrderNumber())
                .recipientName(extractRecipientName(order, firstDelivery))
                .orderItems(orderItemDetails)
                .paymentInfo(paymentInfo)
                .totalOrderPrice(order.getTotalAmount().toString())
                .sellerId(order.getSeller() != null ? order.getSeller().getId() : null)
                .build();
        }

        private static String extractRecipientName(Order order, OrderDelivery delivery) {
            if (delivery != null && delivery.getReceiver() != null) {
                return delivery.getReceiver().getRecipientName();
            }
            return order.getBuyerName();
        }

    }

    @Schema(description = "주문 품목 상세 조회")
    public record OrderItemDetailResponse(

        @Schema(description = "주문번호", example = "ORDER-2025-04-05-00001")
        @NotBlank
        String orderNumber,

        @Valid
        @NotNull
        OrderInfo orderInfo,

        @Valid
        @NotNull
        BuyerInfo buyer,

        @Valid
        @NotNull
        ShippingInfo shipping,

        @Valid
        @NotNull
        OrderItem orderItem

    ) {

        @Schema(description = "주문 정보")
        public record OrderInfo(
            @Schema(description = "주문일", example = "2025-04-05", type = "string", format = "date")
            @NotNull
            @JsonFormat(pattern = "yyyy-MM-dd") // JSON 바디 기준
            String orderDate,

            @Schema(description = "주문상태(예: 반품-상품발송 등)", example = "반품-상품발송")
            @NotBlank
            String orderStatusLabel
        ) {

        }

        @Schema(description = "주문자 정보")
        public record BuyerInfo(
            @Schema(description = "수취인명", example = "홍길동")
            @NotBlank String recipientName,
            @Schema(description = "주문자명", example = "홍길동")
            @NotBlank String buyerName,
            @Schema(description = "주문자번호1", example = "010-1234-5678")
            @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$")
            String buyerPhone1,
            @Schema(description = "주문자번호2", example = "010-9876-5432")
            @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$")
            String buyerPhone2
        ) {

        }

        @Schema(description = "배송 정보")
        public record ShippingInfo(
            @Schema(description = "배송상태", example = "수거중")
            @NotBlank String statusLabel,
            @Schema(description = "택배사", example = "CJ대한통운")
            @NotBlank String courierCompany,
            @Schema(description = "운송장번호", example = "1234-5678-910")
            @Pattern(regexp = "^[A-Za-z0-9-]{5,40}$") String trackingNumber,
            @Schema(description = "배송비(원)", example = "3000")
            @PositiveOrZero Long shippingFee,
            @Schema(description = "배송지 주소", example = "서울시 강남구 예제로 123")
            @NotBlank String address,
            @Schema(description = "배송메모", example = "문 앞에 두세요.")
            @Size(max = 200)
            String memo
        ) {

        }


        @Schema(description = "주문 상품")
        public record OrderItem(
            @Schema(description = "게시글명", example = "예제 상품")
            @NotBlank String boardTitle,
            @Schema(description = "상품명", example = "예제 상품")
            @NotBlank String itemName,
            @Schema(description = "상품수량", example = "2")
            @Positive Integer quantity,
            @Schema(description = "상품금액(원)", example = "50000")
            @PositiveOrZero Long unitPrice,
            @Schema(description = "총 금액(원)", example = "100000")
            @PositiveOrZero Long totalPrice
        ) {

        }
    }

}
