package com.bbangle.bbangle.order.controller.dto.response;

import com.bbangle.bbangle.order.controller.dto.DeliveryStatus;
import com.bbangle.bbangle.order.controller.dto.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public class OrderDetailResponse {
    @Schema(description = "주문 상세 내역")
    public record OrderDetail(
        OrderInfo order,
        BuyerInfo buyer,
        DeliveryInfo delivery,
        ReturnDeliveryInfo returnDelivery,
        ExchangeDeliveryInfo buyerExchangeDelivery,
        ExchangeDeliveryInfo sellerExchangeDelivery,
        List<OrderItemInfo> orderItems
    ) {

        public static OrderDetail sample() {
            return new OrderDetail(
                new OrderInfo(1001L, "ORD-20250927-0001", OrderStatus.PAID, 45000),
                new BuyerInfo("홍길동", "김철수", "010-1234-5678", "02-987-6543"),
                new DeliveryInfo(DeliveryStatus.IN_TRANSIT, "CJ대한통운", "1234567890", 3000,
                    "서울특별시 강남구 테헤란로 123", "부재 시 경비실에 맡겨주세요"),
                new ReturnDeliveryInfo(DeliveryStatus.PICKUP, "롯데택배", "RET-987654321", 2500,
                    "서울특별시 송파구 잠실로 456", "상품 불량으로 반품 요청"),
                new ExchangeDeliveryInfo(DeliveryStatus.PICKUP, "한진택배", "EX-BUYER-11111", 2500,
                    "서울특별시 마포구 독막로 12", "사이즈 교환 요청"),
                new ExchangeDeliveryInfo(DeliveryStatus.IN_TRANSIT, "CJ대한통운", "EX-SELLER-22222", 0,
                    "서울특별시 마포구 독막로 12", "교환상품 발송 완료"),
                List.of(
                    new OrderItemInfo(2001L, "디저트 게시판", "초코 마카롱 세트", 2, 15000),
                    new OrderItemInfo(2002L, "디저트 게시판", "말차 롤케이크", 1, 15000)
                )
            );
        }

        @Schema(description = "주문 정보")
        public record OrderInfo(
            @Schema(description = "주문 ID") Long orderId,
            @Schema(description = "주문 번호") String orderNum,
            @Schema(description = "주문 상태") OrderStatus status,
            @Schema(description = "총 결제 금액") Integer totalPrice
        ) {
        }

        @Schema(description = "구매자 정보")
        public record BuyerInfo(
            @Schema(description = "구매자명") String buyerName,
            @Schema(description = "수취인명") String recipient,
            @Schema(description = "연락처1") String phone1,
            @Schema(description = "연락처2") String phone2
        ) {
        }

        @Schema(description = "배송 정보")
        public record DeliveryInfo(
            @Schema(description = "배송 상태") DeliveryStatus status,
            @Schema(description = "택배사") String deliveryCompany,
            @Schema(description = "운송장 번호") String trackingNumber,
            @Schema(description = "배송비") Integer deliveryFee,
            @Schema(description = "배송지 주소") String address,
            @Schema(description = "배송 메모") String deliveryMemo
        ) {
        }

        @Schema(description = "반품 배송 정보")
        public record ReturnDeliveryInfo(
            @Schema(description = "배송 상태") DeliveryStatus status,
            @Schema(description = "택배사") String deliveryCompany,
            @Schema(description = "운송장 번호") String trackingNumber,
            @Schema(description = "배송비") Integer deliveryFee,
            @Schema(description = "회수지 주소") String address,
            @Schema(description = "배송 메모") String deliveryMemo
        ) {
        }

        @Schema(description = "교환 배송 정보")
        public record ExchangeDeliveryInfo(
            @Schema(description = "배송 상태") DeliveryStatus status,
            @Schema(description = "택배사") String deliveryCompany,
            @Schema(description = "운송장 번호") String trackingNumber,
            @Schema(description = "배송비") Integer deliveryFee,
            @Schema(description = "배송지 주소") String address,
            @Schema(description = "배송 메모") String deliveryMemo
        ) {
        }

        @Schema(description = "주문 상품 정보")
        public record OrderItemInfo(
            @Schema(description = "주문상품 ID") Long orderItemId,
            @Schema(description = "게시판명") String boardName,
            @Schema(description = "상품명") String productName,
            @Schema(description = "수량") Integer quantity,
            @Schema(description = "상품 가격") Integer price
        ) {
        }
    }
}
