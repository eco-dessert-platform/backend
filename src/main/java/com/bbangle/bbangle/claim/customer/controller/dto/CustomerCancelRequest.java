package com.bbangle.bbangle.claim.customer.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CustomerCancelRequest(
    @Schema(description = "취소할 주문 상품 ID 목록", example = "[101, 102]")
    @NotEmpty(message = "취소할 주문 상품은 최소 하나 이상 선택해야 합니다.")
    List<Long> orderItemIds,

    @Schema(description = "취소 사유", example = "단순 변심 / 다른 상품 재구매", maxLength = 500)
    @NotBlank(message = "취소 사유는 필수입니다.")
    @Size(max = 500, message = "취소 사유는 500자 이하로 입력해주세요.")
    String reason
) {}
