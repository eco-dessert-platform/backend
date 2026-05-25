package com.bbangle.bbangle.claim.seller.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ExchangeRejectRequest(
    @Schema(description = "검수 후 반려 사유", example = "고객 귀책으로 인한 상품 파손으로 교환 불가", maxLength = 500)
    @NotBlank(message = "반려 사유는 필수입니다.")
    String reason
) {}
