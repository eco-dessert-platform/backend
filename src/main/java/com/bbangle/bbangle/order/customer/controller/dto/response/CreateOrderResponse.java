package com.bbangle.bbangle.order.customer.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 주문 생성 응답.
 *
 * <p>{@code paymentNumber} · {@code orderName} · {@code totalAmount} 를 그대로 PG 결제창에 전달한다.
 */
@Schema(description = "주문 생성 응답")
public record CreateOrderResponse(

    @Schema(description = "결제번호. PG 에 전달하는 orderId 다.", example = "PAY-20260924-7K3XQ2M9")
    String paymentNumber,

    @Schema(description = "PG 결제창 표시용 주문명", example = "글루텐프리 케이크 외 2건")
    String orderName,

    @Schema(description = "최종 결제금액", example = "24400")
    long totalAmount,

    @Schema(description = "스토어별 주문")
    List<StoreOrderResponse> orders
) {

    @Schema(description = "스토어 단위 주문")
    public record StoreOrderResponse(
        @Schema(description = "주문 Id", example = "11")
        Long orderId,

        @Schema(description = "주문번호", example = "ORDER-20260924-A1B2C3D4")
        String orderNumber,

        @Schema(description = "스토어 Id", example = "1")
        Long storeId,

        @Schema(description = "스토어명", example = "빵그리의 오븐")
        String storeName,

        @Schema(description = "상품금액(정가 합계)", example = "20000")
        long productAmount,

        @Schema(description = "상품 할인금액", example = "1600")
        long discountAmount,

        @Schema(description = "배송비", example = "3000")
        long deliveryFee,

        @Schema(description = "해당 스토어 결제금액", example = "21400")
        long totalAmount
    ) {
    }
}
