package com.bbangle.bbangle.order.seller.controller.dto.request;

import com.bbangle.bbangle.claim.seller.service.model.ExchangeCreateCommand;
import com.bbangle.bbangle.claim.seller.service.model.ReturnCreateCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderConfirmCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.ShipmentRegisterCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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

    @Schema(description = "판매자 운송장 정보 등록 요청 DTO")
    public record ShipmentRegisterRequest(

        @Schema(description = "운송장 등록 대상 주문상품 ID 목록")
        @NotEmpty(message = "orderItemIds는 필수입니다.")
        List<Long> orderItemIds,

        @Schema(description = "택배사명", example = "CJ대한통운")
        @NotBlank(message = "택배사명은 필수입니다.")
        String courierName,

        @Schema(description = "운송장번호", example = "1234567890")
        @NotBlank(message = "운송장번호는 필수입니다.")
        String trackingNumber

    ) {

        public ShipmentRegisterCommand toCommand(Long sellerId, Long orderId) {
            return ShipmentRegisterCommand.builder()
                .sellerId(sellerId)
                .orderId(orderId)
                .orderItemIds(orderItemIds)
                .courierName(courierName)
                .trackingNumber(trackingNumber)
                .build();
        }
    }

    @Schema(description = "판매자 반품 요청 생성 DTO")
    public record ReturnCreateRequest(

        @Schema(description = "반품 대상 주문상품 ID 목록")
        @NotEmpty(message = "orderItemIds는 필수입니다.")
        List<Long> orderItemIds,

        @Schema(description = "반품 사유")
        String reason,

        @Schema(description = "판매자 코멘트")
        String sellerComment

    ) {

        public ReturnCreateCommand toCommand(Long sellerId, Long orderId) {
            return ReturnCreateCommand.builder()
                .sellerId(sellerId)
                .orderId(orderId)
                .orderItemIds(orderItemIds)
                .reason(reason)
                .sellerComment(sellerComment)
                .build();
        }
    }

    @Schema(description = "판매자 교환 요청 생성 DTO")
    public record ExchangeCreateRequest(

        @Schema(description = "교환 대상 주문상품 ID 목록")
        @NotEmpty(message = "orderItemIds는 필수입니다.")
        List<Long> orderItemIds,

        @Schema(description = "교환 사유")
        String reason,

        @Schema(description = "판매자 코멘트")
        String sellerComment

    ) {

        public ExchangeCreateCommand toCommand(Long sellerId, Long orderId) {
            return ExchangeCreateCommand.builder()
                .sellerId(sellerId)
                .orderId(orderId)
                .orderItemIds(orderItemIds)
                .reason(reason)
                .sellerComment(sellerComment)
                .build();
        }
    }
}
