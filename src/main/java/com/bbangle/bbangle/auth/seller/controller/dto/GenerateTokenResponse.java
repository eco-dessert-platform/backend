package com.bbangle.bbangle.auth.seller.controller.dto;


import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Token 발급 응답 DTO")
public record GenerateTokenResponse(
    @Schema(description = "Seller Id") Long sellerId,
    @Schema(description = "Seller 계정 상태") CertificationStatus status
) {
}
