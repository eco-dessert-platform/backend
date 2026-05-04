package com.bbangle.bbangle.order.seller.controller.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "결제 정보")
public record PaymentInfo(

    @Schema(description = "결제상태", example = "결제완료")
    String paymentStatus,

    @Schema(description = "결제수단", example = "신용/체크카드")
    String paymentMethod,

    @Schema(description = "결제일", example = "2025-03-01")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDateTime paidAt
) {
    public static PaymentInfo of(String paymentStatus, String paymentMethod, LocalDateTime paidAt) {
        return new PaymentInfo(paymentStatus, paymentMethod, paidAt);
    }
}
