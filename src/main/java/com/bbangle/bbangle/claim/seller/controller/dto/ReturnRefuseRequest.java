package com.bbangle.bbangle.claim.seller.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "반품 거절(반려) 요청 DTO")
public record ReturnRefuseRequest(

    @Schema(description = "거절 사유", example = "반품 불가 상품입니다.", maxLength = 500)
    @NotBlank
    @Size(max = 500)
    String refusalReason

) {
}
