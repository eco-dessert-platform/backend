package com.bbangle.bbangle.order.seller.controller.dto.response;

import com.bbangle.bbangle.order.domain.OrderDelivery;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.CourierCompany;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.seller.controller.model.OrderItemInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrderItemListResponse {

    @Schema(description = "주문 품목 리스트")
    public record OrderItemList(

        @Schema(description = "주문번호")
        @NotBlank
        String orderNumber,

        @Schema(description = "주문상태(예: 반품-상품발송 등)")
        @NotBlank
        OrderStatus orderStatus,

        @Valid
        @NotNull
        @Schema(description = "주문 상품 정보")
        OrderItemInfo orderItemInfo,

        @Schema(description = "배송상태")
        OrderDeliveryStatus orderDeliveryStatus,

        @Schema(description = "택배사")
        CourierCompany courierCompany,

        @Schema(description = "운송장 번호")
        String trackingNumber

    ) {

        public static OrderItemList from(String orderNumber, OrderItem orderItem, OrderDelivery latestDelivery) {
            return new OrderItemList(
                orderNumber,
                orderItem.getOrderStatus(),
                new OrderItemInfo(
                    extractItemName(orderItem),
                    orderItem.getQuantity(),
                    orderItem.getUnitPrice() != null ? orderItem.getUnitPrice().longValue() : 0L,
                    orderItem.getTotalPrice() != null ? orderItem.getTotalPrice().longValue() : 0L
                ),
                extractDeliveryStatus(latestDelivery),
                extractCourierCompany(latestDelivery),
                extractTrackingNumber(latestDelivery)
            );
        }

        private static String extractItemName(OrderItem orderItem) {
            return orderItem.getProduct() != null ? orderItem.getProduct().getTitle() : "-";
        }

        private static OrderDeliveryStatus extractDeliveryStatus(OrderDelivery delivery) {
            if (delivery != null) {
                return delivery.getStatus();
            }
            return OrderDeliveryStatus.PREPARING;
        }

        private static CourierCompany extractCourierCompany(OrderDelivery delivery) {
            if (delivery != null && delivery.getShipping() != null) {
                return CourierCompany.fromDisplayName(delivery.getShipping().getCourierName());
            }
            return CourierCompany.NONE;
        }

        private static String extractTrackingNumber(OrderDelivery delivery) {
            if (delivery != null && delivery.getShipping() != null) {
                return delivery.getShipping().getTrackingNumber();
            }
            return "-";
        }
    }
}
