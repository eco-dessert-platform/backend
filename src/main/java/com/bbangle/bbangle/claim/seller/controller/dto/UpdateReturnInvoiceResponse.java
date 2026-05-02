package com.bbangle.bbangle.claim.seller.controller.dto;

import com.bbangle.bbangle.claim.domain.ClaimDelivery;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "반품 수거 운송장 수정 응답 DTO")
public record UpdateReturnInvoiceResponse(

    @Schema(description = "반품 요청 ID", example = "1")
    Long returnId,

    @Schema(description = "택배사 명", example = "CJ대한통운")
    String courierName,

    @Schema(description = "운송장 번호", example = "1234567890")
    String trackingNumber

) {

    public static UpdateReturnInvoiceResponse of(Long returnId, ClaimDelivery delivery) {
        return new UpdateReturnInvoiceResponse(
            returnId,
            delivery.getShipping().getCourierName(),
            delivery.getShipping().getTrackingNumber()
        );
    }
}
