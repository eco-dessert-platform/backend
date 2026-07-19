package com.bbangle.bbangle.claim.seller.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ExchangeHoldRequest(
    @Schema(description = "보류 사유", example = "교환 배송비 미입금으로 인한 보류", maxLength = 500)
    @NotBlank(message = "보류 사유는 필수입니다.")
    String reason
) {}
