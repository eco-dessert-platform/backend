package com.bbangle.bbangle.claim.customer.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CustomerReturnRequest(
    @Schema(description = "반품 신청할 주문 상품 ID 목록", example = "[201, 202]")
    @NotEmpty(message = "반품할 주문 상품은 최소 하나 이상 선택해야 합니다.")
    List<Long> orderItemIds,

    @Schema(description = "반품 사유", example = "단순 변심 / 사이즈 불일치", maxLength = 500)
    @NotBlank(message = "반품 사유는 필수입니다.")
    String reason
) {}
