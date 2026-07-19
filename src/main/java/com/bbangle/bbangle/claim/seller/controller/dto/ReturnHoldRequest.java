package com.bbangle.bbangle.claim.seller.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "반품 보류 요청 DTO")
public record ReturnHoldRequest(

    @Schema(description = "보류 사유", example = "배송비 미입금 확인 중입니다.", maxLength = 500)
    @NotBlank
    @Size(max = 500)
    String holdReason

) {
}
