package com.bbangle.bbangle.auth.seller.controller.dto;

import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record GenerateTokenResponse(
    @Schema(description = "Refresh 토큰") String refreshToken,
    @Schema(description = "Access 토큰") String accessToken,
    @Schema(description = "Seller Id") Long sellerId,
    @Schema(description = "Seller 계정 상태") CertificationStatus status
    ) {

    public static GenerateTokenResponse of(
        String refreshToken,
        String accessToken,
        Long sellerId,
        CertificationStatus status
    ) {
        return new GenerateTokenResponse(refreshToken, accessToken, sellerId, status);
    }
}
